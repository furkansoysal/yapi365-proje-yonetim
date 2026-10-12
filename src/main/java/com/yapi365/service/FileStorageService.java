package com.yapi365.service;

import com.yapi365.config.StorageProperties;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Dosyaların birincil kopyası sunucu diskinde (Docker volume) tutulur, SharePoint'e ayrıca aktarılır.
 * Böylece Microsoft 365 geçici olarak erişilemez olsa bile sistem çalışmaya devam eder.
 * Dosyalar akış (stream) olarak yazılır; büyük dosyalar belleğe alınmaz.
 */
@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(StorageProperties props) throws IOException {
        this.root = Path.of(props.dir()).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public StoredFile store(InputStream in, String relativePath) throws IOException {
        Path target = resolve(relativePath);
        Files.createDirectories(target.getParent());
        MessageDigest sha = sha256();
        long size;
        try (DigestInputStream din = new DigestInputStream(in, sha);
             OutputStream out = Files.newOutputStream(target)) {
            size = din.transferTo(out);
        }
        return new StoredFile(relativePath, size, HexFormat.of().formatHex(sha.digest()));
    }

    public Path path(String relativePath) {
        return resolve(relativePath);
    }

    public Resource load(String relativePath) {
        return new PathResource(resolve(relativePath));
    }

    private Path resolve(String relativePath) {
        Path p = root.resolve(relativePath).normalize();
        if (!p.startsWith(root)) {
            throw new IllegalArgumentException("Geçersiz dosya yolu");
        }
        return p;
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
