package com.yapi365.repo;

import com.yapi365.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByProjectIdOrderByFolderPathAscTitleAsc(Long projectId);
    Optional<Document> findByProjectIdAndFolderPathAndTitle(Long projectId, String folderPath, String title);
    long countByProjectId(Long projectId);
}
