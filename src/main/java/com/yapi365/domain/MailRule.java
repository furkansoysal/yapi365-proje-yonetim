package com.yapi365.domain;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

/** Arayüzden yönetilen mail sınıflandırma kuralı. Koşullar AND ile birleşir, boş koşul yok sayılır. */
@Entity
@Table(name = "mail_rule")
@Audited
public class MailRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Küçük sayı önce değerlendirilir. */
    @Column(nullable = false)
    private int priority = 100;

    @Column(name = "sender_contains")
    private String senderContains;

    @Column(name = "subject_contains")
    private String subjectContains;

    /** Konu veya gövdede geçmesi gereken anahtar kelimeler, virgülle ayrılır (herhangi biri). */
    private String keywords;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private MailActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_module", length = 20)
    private AppModule targetModule;

    @Column(name = "target_folder")
    private String targetFolder;

    @Column(name = "notify_email")
    private String notifyEmail;

    private boolean active = true;

    public MailRule() {
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getSenderContains() { return senderContains; }
    public void setSenderContains(String senderContains) { this.senderContains = senderContains; }
    public String getSubjectContains() { return subjectContains; }
    public void setSubjectContains(String subjectContains) { this.subjectContains = subjectContains; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public MailActionType getActionType() { return actionType; }
    public void setActionType(MailActionType actionType) { this.actionType = actionType; }
    public AppModule getTargetModule() { return targetModule; }
    public void setTargetModule(AppModule targetModule) { this.targetModule = targetModule; }
    public String getTargetFolder() { return targetFolder; }
    public void setTargetFolder(String targetFolder) { this.targetFolder = targetFolder; }
    public String getNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(String notifyEmail) { this.notifyEmail = notifyEmail; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
