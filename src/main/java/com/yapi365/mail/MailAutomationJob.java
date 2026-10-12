package com.yapi365.mail;

import com.yapi365.domain.MailLog;
import com.yapi365.domain.MailRule;
import com.yapi365.graph.GraphMailClient;
import com.yapi365.repo.MailLogRepository;
import com.yapi365.repo.MailRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Paylaşılan posta kutusunu periyodik olarak okur, sınıflandırır ve aksiyon alır. */
@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class MailAutomationJob {

    private static final Logger log = LoggerFactory.getLogger(MailAutomationJob.class);

    private final GraphMailClient mailClient;
    private final MailRuleRepository rules;
    private final MailLogRepository logs;
    private final MailRuleEngine engine;
    private final MailClassifier classifier;
    private final MailActionExecutor executor;

    public MailAutomationJob(GraphMailClient mailClient, MailRuleRepository rules, MailLogRepository logs,
                             MailRuleEngine engine, MailClassifier classifier, MailActionExecutor executor) {
        this.mailClient = mailClient;
        this.rules = rules;
        this.logs = logs;
        this.engine = engine;
        this.classifier = classifier;
        this.executor = executor;
    }

    @Scheduled(fixedDelayString = "${app.mail.poll-interval-ms:120000}")
    public void poll() {
        List<MailRule> active = rules.findByActiveTrueOrderByPriorityAsc();
        for (MailMessage mail : mailClient.unread(25)) {
            if (logs.existsByGraphMessageId(mail.id())) {
                continue;
            }
            try {
                Optional<MailRule> rule = engine.match(active, mail);
                MailLog entry;
                if (rule.isPresent()) {
                    String result = executor.execute(rule.get(), mail);
                    entry = new MailLog(mail.id(), mail.receivedAt(), mail.sender(), mail.subject(),
                            rule.get().getName(), "KURAL", result);
                } else {
                    String suggestion = classifier.classify(mail)
                            .map(m -> "LLM önerisi: " + m.getLabel() + " (onay bekliyor)")
                            .orElse("Sınıflandırılamadı - manuel inceleme");
                    entry = new MailLog(mail.id(), mail.receivedAt(), mail.sender(), mail.subject(),
                            null, "LLM", suggestion);
                }
                logs.save(entry);
                mailClient.markRead(mail.id());
            } catch (RuntimeException ex) {
                log.warn("Mail işlenemedi {}: {}", mail.id(), ex.getMessage());
            }
        }
    }
}
