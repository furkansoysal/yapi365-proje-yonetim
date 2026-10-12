package com.yapi365.domain;

import com.yapi365.security.AuditRevisionListener;
import jakarta.persistence.*;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.time.Instant;

/** Her değişiklik (Envers revizyonu) için kim / ne zaman bilgisi. */
@Entity
@Table(name = "audit_revision")
@RevisionEntity(AuditRevisionListener.class)
public class AuditRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private Long id;

    @RevisionTimestamp
    @Column(name = "rev_timestamp", nullable = false)
    private long timestamp;

    @Column(length = 200)
    private String username;

    public Long getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public Instant getInstant() { return Instant.ofEpochMilli(timestamp); }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
