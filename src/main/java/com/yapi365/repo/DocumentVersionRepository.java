package com.yapi365.repo;

import com.yapi365.domain.DocumentVersion;
import com.yapi365.domain.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
    List<DocumentVersion> findByDocumentIdOrderByVersionNoDesc(Long documentId);
    List<DocumentVersion> findTop20BySyncStatusAndSyncAttemptsLessThanOrderByIdAsc(SyncStatus status, int maxAttempts);
    long countByDocumentProjectIdAndSyncStatusIn(Long projectId, List<SyncStatus> statuses);
}
