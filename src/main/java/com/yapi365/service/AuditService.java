package com.yapi365.service;

import com.yapi365.domain.AuditRevision;
import com.yapi365.domain.Contract;
import com.yapi365.domain.Document;
import com.yapi365.domain.MailRule;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/** Hibernate Envers üzerinden "kim, ne zaman, ne değiştirdi" sorguları. */
@Service
public class AuditService {

    public record Entry(long revision, Instant when, String user, String entity, String label, String action) {
    }

    public record Change(String field, Object before, Object after) {
    }

    public record HistoryItem(Instant when, String user, String action, List<Change> changes) {
    }

    private final EntityManager em;

    public AuditService(EntityManager em) {
        this.em = em;
    }

    @Transactional(readOnly = true)
    public List<Entry> recent(int max) {
        AuditReader reader = AuditReaderFactory.get(em);
        List<Entry> all = new ArrayList<>();
        collect(reader, Contract.class, "Sözleşme", c -> c.getContractNo() + " - " + c.getCounterparty(), max, all);
        collect(reader, Document.class, "Doküman", d -> d.getFolderPath() + " / " + d.getTitle() + " (v" + d.getCurrentVersion() + ")", max, all);
        collect(reader, MailRule.class, "Mail Kuralı", MailRule::getName, max, all);
        return all.stream()
                .sorted(Comparator.comparingLong(Entry::revision).reversed())
                .limit(max)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private <T> void collect(AuditReader reader, Class<T> type, String entityName,
                             Function<T, String> label, int max, List<Entry> out) {
        List<Object[]> rows = reader.createQuery()
                .forRevisionsOfEntity(type, false, true)
                .addOrder(AuditEntity.revisionNumber().desc())
                .setMaxResults(max)
                .getResultList();
        for (Object[] row : rows) {
            T entity = (T) row[0];
            AuditRevision rev = (AuditRevision) row[1];
            RevisionType rt = (RevisionType) row[2];
            String text;
            try {
                text = label.apply(entity);
            } catch (RuntimeException e) {
                text = "(silinmiş kayıt)";
            }
            out.add(new Entry(rev.getId(), rev.getInstant(), rev.getUsername(), entityName, text, actionLabel(rt)));
        }
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<HistoryItem> contractHistory(Long contractId) {
        List<Object[]> rows = AuditReaderFactory.get(em).createQuery()
                .forRevisionsOfEntity(Contract.class, false, true)
                .add(AuditEntity.id().eq(contractId))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();
        List<HistoryItem> result = new ArrayList<>();
        Map<String, Object> previous = null;
        for (Object[] row : rows) {
            Contract c = (Contract) row[0];
            AuditRevision rev = (AuditRevision) row[1];
            Map<String, Object> snap = c.snapshot();
            List<Change> changes = new ArrayList<>();
            for (Map.Entry<String, Object> e : snap.entrySet()) {
                Object before = previous == null ? null : previous.get(e.getKey());
                if (!Objects.equals(before, e.getValue())) {
                    changes.add(new Change(e.getKey(), before, e.getValue()));
                }
            }
            result.add(new HistoryItem(rev.getInstant(), rev.getUsername(), actionLabel((RevisionType) row[2]), changes));
            previous = snap;
        }
        java.util.Collections.reverse(result);
        return result;
    }

    private static String actionLabel(RevisionType rt) {
        return switch (rt) {
            case ADD -> "Oluşturdu";
            case MOD -> "Güncelledi";
            case DEL -> "Sildi";
        };
    }
}
