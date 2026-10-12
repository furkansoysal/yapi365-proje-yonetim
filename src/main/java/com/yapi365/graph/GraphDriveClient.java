package com.yapi365.graph;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SharePoint / OneDrive dosya yükleme.
 * 4 MB altı dosyalar tek istekte, büyük dosyalar "upload session" ile parça parça yüklenir
 * (Graph sınırı 250 GB; parça boyutu 320 KiB'nin katı olmalıdır).
 */
@Component
public class GraphDriveClient {

    static final long SIMPLE_UPLOAD_LIMIT = 4L * 1024 * 1024;
    static final int CHUNK_SIZE = 32 * 320 * 1024; // 10 MiB

    private static final String GRAPH = "https://graph.microsoft.com/v1.0";

    private final GraphProperties props;
    private final GraphTokenProvider tokens;
    private final RestClient graph = RestClient.create();
    /** Upload session URL'leri önceden imzalıdır; Authorization başlığı GÖNDERİLMEMELİ. */
    private final RestClient anonymous = RestClient.create();

    public GraphDriveClient(GraphProperties props, GraphTokenProvider tokens) {
        this.props = props;
        this.tokens = tokens;
    }

    public DriveItemRef upload(String remotePath, Path localFile) throws IOException {
        long size = Files.size(localFile);
        String encoded = encodePath(remotePath);
        String itemUrl = GRAPH + "/drives/" + props.driveId() + "/root:/" + encoded;
        return size <= SIMPLE_UPLOAD_LIMIT
                ? simpleUpload(itemUrl, localFile)
                : sessionUpload(itemUrl, localFile, size);
    }

    private DriveItemRef simpleUpload(String itemUrl, Path file) throws IOException {
        Map<?, ?> item = graph.put()
                .uri(URI.create(itemUrl + ":/content"))
                .header("Authorization", "Bearer " + tokens.token())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(Files.readAllBytes(file))
                .retrieve()
                .body(Map.class);
        return toRef(item);
    }

    private DriveItemRef sessionUpload(String itemUrl, Path file, long size) throws IOException {
        Map<?, ?> session = graph.post()
                .uri(URI.create(itemUrl + ":/createUploadSession"))
                .header("Authorization", "Bearer " + tokens.token())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("item", Map.of("@microsoft.graph.conflictBehavior", "replace")))
                .retrieve()
                .body(Map.class);
        if (session == null || session.get("uploadUrl") == null) {
            throw new IOException("Upload session açılamadı");
        }
        URI uploadUrl = URI.create((String) session.get("uploadUrl"));

        byte[] buffer = new byte[CHUNK_SIZE];
        long offset = 0;
        Map<?, ?> last = null;
        try (InputStream in = Files.newInputStream(file)) {
            while (offset < size) {
                int read = in.readNBytes(buffer, 0, (int) Math.min(CHUNK_SIZE, size - offset));
                if (read <= 0) {
                    throw new IOException("Dosya beklenenden kısa: " + file);
                }
                byte[] chunk = read == buffer.length ? buffer : Arrays.copyOf(buffer, read);
                long end = offset + read - 1;
                last = anonymous.put()
                        .uri(uploadUrl)
                        .header("Content-Range", "bytes " + offset + "-" + end + "/" + size)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(chunk)
                        .retrieve()
                        .body(Map.class);
                offset += read;
            }
        }
        return toRef(last);
    }

    private static DriveItemRef toRef(Map<?, ?> item) {
        if (item == null || item.get("id") == null) {
            throw new IllegalStateException("Graph yanıtında dosya bilgisi yok");
        }
        return new DriveItemRef((String) item.get("id"), (String) item.get("webUrl"));
    }

    static String encodePath(String path) {
        return Arrays.stream(path.split("/"))
                .filter(s -> !s.isBlank())
                .map(s -> UriUtils.encodePathSegment(s, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));
    }
}
