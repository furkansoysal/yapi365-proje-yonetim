package com.yapi365.mail;

import com.yapi365.domain.MailRule;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Kural tabanlı sınıflandırma. Kurallar önceliğe göre denenir; ilk eşleşen kazanır.
 * Bir kuralda doldurulmuş tüm koşullar sağlanmalıdır (AND). Anahtar kelimelerden herhangi biri yeterlidir (OR).
 * Hiçbir kural eşleşmezse {@link MailClassifier} (LLM) devreye girer.
 */
@Component
public class MailRuleEngine {

    private static final Locale TR = Locale.forLanguageTag("tr");

    public Optional<MailRule> match(List<MailRule> rules, MailMessage mail) {
        return rules.stream()
                .filter(MailRule::isActive)
                .sorted(Comparator.comparingInt(MailRule::getPriority))
                .filter(r -> matches(r, mail))
                .findFirst();
    }

    boolean matches(MailRule r, MailMessage m) {
        if (notBlank(r.getSenderContains()) && !contains(m.sender(), r.getSenderContains())) {
            return false;
        }
        if (notBlank(r.getSubjectContains()) && !contains(m.subject(), r.getSubjectContains())) {
            return false;
        }
        if (notBlank(r.getKeywords())) {
            String text = norm(m.subject()) + " " + norm(m.body());
            boolean any = false;
            for (String k : r.getKeywords().split(",")) {
                if (!k.isBlank() && text.contains(norm(k.trim()))) {
                    any = true;
                    break;
                }
            }
            if (!any) {
                return false;
            }
        }
        // Hiç koşulu olmayan kural her maile eşleşmesin
        return notBlank(r.getSenderContains()) || notBlank(r.getSubjectContains()) || notBlank(r.getKeywords());
    }

    private static boolean contains(String haystack, String needle) {
        return norm(haystack).contains(norm(needle));
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(TR);
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
