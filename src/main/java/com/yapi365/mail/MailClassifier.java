package com.yapi365.mail;

import com.yapi365.domain.AppModule;

import java.util.Optional;

/**
 * Kurallara uymayan mailler için yapay zekâ (LLM) destekli sınıflandırma noktası.
 * Canlıda bu arayüzün bir LLM implementasyonu (Azure OpenAI / OpenAI / Anthropic) eklenir;
 * model yalnızca modül önerisi döner, aksiyon yine kural/onay mekanizmasıyla çalışır.
 */
public interface MailClassifier {

    Optional<AppModule> classify(MailMessage mail);
}
