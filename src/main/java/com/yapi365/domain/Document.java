package com.yapi365.domain;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "document")
@Audited
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Project project;

    /** Klasör yolu, örn. "Sözleşmeler/Alt Yüklenici". SharePoint'te aynı yapı oluşturulur. */
    @Column(name = "folder_path", nullable = false)
    private String folderPath;

    @Column(nullable = false)
    private String title;

    @Column(name = "current_version", nullable = false)
    private int currentVersion;

    protected Document() {
    }

    public Document(Project project, String folderPath, String title) {
        this.project = project;
        this.folderPath = folderPath;
        this.title = title;
    }

    public int nextVersion() {
        return ++currentVersion;
    }

    public Long getId() { return id; }
    public Project getProject() { return project; }
    public String getFolderPath() { return folderPath; }
    public String getTitle() { return title; }
    public int getCurrentVersion() { return currentVersion; }
}
