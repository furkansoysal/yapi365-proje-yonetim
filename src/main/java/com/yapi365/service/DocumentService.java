package com.yapi365.service;

import com.yapi365.domain.Document;
import com.yapi365.domain.DocumentVersion;
import com.yapi365.domain.Project;
import com.yapi365.graph.GraphProperties;
import com.yapi365.repo.DocumentRepository;
import com.yapi365.repo.DocumentVersionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documents;
    private final DocumentVersionRepository versions;
    private final FileStorageService storage;
    private final GraphProperties graph;
    private final ApplicationEventPublisher events;

    public DocumentService(DocumentRepository documents, DocumentVersionRepository versions,
                           FileStorageService storage, GraphProperties graph, ApplicationEventPublisher events) {
        this.documents = documents;
        this.versions = versions;
        this.storage = storage;
        this.graph = graph;
        this.events = events;
    }

    /**
     * Aynı klasörde aynı başlıkla yüklenen dosya yeni versiyon olarak eklenir.
     * Kayıt commit edildikten sonra SharePoint aktarımı arka planda başlar.
     */
    @Transactional
    public DocumentVersion upload(Project project, String folder, String title, String fileName,
                                  String contentType, InputStream content, String username) throws IOException {
        String cleanFolder = normalizeFolder(folder);
        Document doc = documents.findByProjectIdAndFolderPathAndTitle(project.getId(), cleanFolder, title)
                .orElseGet(() -> documents.save(new Document(project, cleanFolder, title)));
        int versionNo = doc.nextVersion();

        String relative = project.getCode() + "/" + doc.getId() + "/v" + versionNo + "_"
                + UUID.randomUUID().toString().substring(0, 8) + "_" + safeName(fileName);
        StoredFile stored = storage.store(content, relative);

        DocumentVersion v = versions.save(new DocumentVersion(doc, versionNo, fileName, contentType,
                stored.size(), stored.sha256(), stored.relativePath(), username));
        if (graph.enabled()) {
            events.publishEvent(new DocumentUploadedEvent(v.getId()));
        } else {
            v.markDisabled();
        }
        return v;
    }

    static String normalizeFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            return "Genel";
        }
        return folder.trim().replace('\\', '/').replaceAll("/{2,}", "/").replaceAll("^/|/$", "");
    }

    static String safeName(String name) {
        return name == null ? "dosya" : name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
