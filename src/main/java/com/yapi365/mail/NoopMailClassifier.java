package com.yapi365.mail;

import com.yapi365.domain.AppModule;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Varsayılan: LLM kapalıyken kural dışı mailler "sınıflandırılamadı" olarak işaretlenir. */
@Component
public class NoopMailClassifier implements MailClassifier {

    @Override
    public Optional<AppModule> classify(MailMessage mail) {
        return Optional.empty();
    }
}
