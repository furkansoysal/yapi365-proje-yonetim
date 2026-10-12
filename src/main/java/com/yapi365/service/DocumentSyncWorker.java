package com.yapi365.service;

import com.yapi365.domain.DocumentVersion;
import com.yapi365.domain.SyncStatus;
import com.yapi365.repo.DocumentVersionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Senkron işinin veritabanı adımları. Dosya aktarımı transaction dışında yapılır (uzun süren yüklemeler bağlantı tutmasın). */
@Component
public class DocumentSyncWorker {

    public static final int MAX_ATTEMPTS = 5;

    private final DocumentVersionRepository versions;

    public DocumentSyncWorker(DocumentVersionRepository versions) {
        this.versions = versions;
    }

    public record SyncJob(Long versionId, String storagePath, String remotePath) {
    }

    @Transactional(readOnly = true)
    public SyncJob prepare(Long versionId, String rootFolder) {
        DocumentVersion v = versions.findById(versionId).orElseThrow();
        String remote = rootFolder + "/" + v.getDocument().getProject().getCode() + "/"
                + v.getDocument().getFolderPath() + "/" + v.getDocument().getTitle()
                + "/v" + v.getVersionNo() + "_" + v.getFileName();
        return new SyncJob(v.getId(), v.getStoragePath(), remote);
    }

    @Transactional
    public void complete(Long versionId, String itemId, String webUrl) {
        versions.findById(versionId).ifPresent(v -> v.markSynced(itemId, webUrl));
    }

    @Transactional
    public void fail(Long versionId, String error) {
        versions.findById(versionId).ifPresent(v -> v.markFailed(error));
    }

    @Transactional(readOnly = true)
    public List<Long> retryable() {
        return versions.findTop20BySyncStatusAndSyncAttemptsLessThanOrderByIdAsc(SyncStatus.FAILED, MAX_ATTEMPTS)
                .stream().map(DocumentVersion::getId).toList();
    }
}
