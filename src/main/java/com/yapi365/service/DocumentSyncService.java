package com.yapi365.service;

import com.yapi365.graph.DriveItemRef;
import com.yapi365.graph.GraphDriveClient;
import com.yapi365.graph.GraphProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Yüklenen her versiyonu SharePoint/OneDrive'a aktarır; hata olursa artan aralıklarla tekrar dener. */
@Service
public class DocumentSyncService {

    private static final Logger log = LoggerFactory.getLogger(DocumentSyncService.class);

    private final DocumentSyncWorker worker;
    private final GraphDriveClient drive;
    private final FileStorageService storage;
    private final GraphProperties graph;

    public DocumentSyncService(DocumentSyncWorker worker, GraphDriveClient drive,
                               FileStorageService storage, GraphProperties graph) {
        this.worker = worker;
        this.drive = drive;
        this.storage = storage;
        this.graph = graph;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUploaded(DocumentUploadedEvent event) {
        sync(event.versionId());
    }

    @Scheduled(fixedDelayString = "${app.graph.retry-interval-ms:300000}", initialDelay = 60000)
    public void retryFailed() {
        if (!graph.enabled()) {
            return;
        }
        worker.retryable().forEach(this::sync);
    }

    void sync(Long versionId) {
        try {
            DocumentSyncWorker.SyncJob job = worker.prepare(versionId, graph.rootFolder());
            DriveItemRef ref = drive.upload(job.remotePath(), storage.path(job.storagePath()));
            worker.complete(versionId, ref.id(), ref.webUrl());
            log.info("SharePoint aktarımı tamam: versiyon {} -> {}", versionId, ref.webUrl());
        } catch (Exception ex) {
            log.warn("SharePoint aktarımı başarısız: versiyon {}: {}", versionId, ex.getMessage());
            worker.fail(versionId, ex.getMessage());
        }
    }
}
