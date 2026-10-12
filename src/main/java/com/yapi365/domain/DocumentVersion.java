package com.yapi365.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "document_version")
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "version_no", nullable = false)
    private int versionNo;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(nullable = false, length = 64)
    private String sha256;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    private SyncStatus syncStatus = SyncStatus.PENDING;

    @Column(name = "sync_attempts", nullable = false)
    private int syncAttempts;

    @Column(name = "sync_error", length = 1000)
    private String syncError;

    @Column(name = "graph_item_id")
    private String graphItemId;

    @Column(name = "web_url", length = 1000)
    private String webUrl;

    protected DocumentVersion() {
    }

    public DocumentVersion(Document document, int versionNo, String fileName, String contentType,
                           long sizeBytes, String sha256, String storagePath, String uploadedBy) {
        this.document = document;
        this.versionNo = versionNo;
        this.fileName = fileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.sha256 = sha256;
        this.storagePath = storagePath;
        this.uploadedBy = uploadedBy;
    }

    public void markSynced(String itemId, String url) {
        this.syncStatus = SyncStatus.SYNCED;
        this.graphItemId = itemId;
        this.webUrl = url;
        this.syncError = null;
    }

    public void markFailed(String error) {
        this.syncStatus = SyncStatus.FAILED;
        this.syncAttempts++;
        this.syncError = error == null ? null : error.substring(0, Math.min(error.length(), 1000));
    }

    public void markDisabled() {
        this.syncStatus = SyncStatus.DISABLED;
    }

    public Long getId() { return id; }
    public Document getDocument() { return document; }
    public int getVersionNo() { return versionNo; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public String getSha256() { return sha256; }
    public String getStoragePath() { return storagePath; }
    public String getUploadedBy() { return uploadedBy; }
    public Instant getUploadedAt() { return uploadedAt; }
    public SyncStatus getSyncStatus() { return syncStatus; }
    public int getSyncAttempts() { return syncAttempts; }
    public String getSyncError() { return syncError; }
    public String getGraphItemId() { return graphItemId; }
    public String getWebUrl() { return webUrl; }
}
