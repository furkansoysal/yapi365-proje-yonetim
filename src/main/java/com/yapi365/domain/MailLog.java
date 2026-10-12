package com.yapi365.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "mail_log")
public class MailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "graph_message_id", unique = true)
    private String graphMessageId;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false, length = 500)
    private String subject;

    @Column(name = "matched_rule")
    private String matchedRule;

    /** KURAL veya LLM */
    @Column(name = "classified_by", length = 10)
    private String classifiedBy;

    @Column(name = "action_result", length = 1000)
    private String actionResult;

    protected MailLog() {
    }

    public MailLog(String graphMessageId, Instant receivedAt, String sender, String subject,
                   String matchedRule, String classifiedBy, String actionResult) {
        this.graphMessageId = graphMessageId;
        this.receivedAt = receivedAt;
        this.sender = sender;
        this.subject = subject;
        this.matchedRule = matchedRule;
        this.classifiedBy = classifiedBy;
        this.actionResult = actionResult;
    }

    public Long getId() { return id; }
    public String getGraphMessageId() { return graphMessageId; }
    public Instant getReceivedAt() { return receivedAt; }
    public String getSender() { return sender; }
    public String getSubject() { return subject; }
    public String getMatchedRule() { return matchedRule; }
    public String getClassifiedBy() { return classifiedBy; }
    public String getActionResult() { return actionResult; }
}
