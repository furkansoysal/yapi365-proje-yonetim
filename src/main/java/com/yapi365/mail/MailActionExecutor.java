package com.yapi365.mail;

import com.yapi365.domain.MailRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Eşleşen kuralın aksiyonunu çalıştırır.
 * İskelette aksiyonlar kayıt altına alınır; modüller geliştikçe her aksiyon ilgili servise bağlanır
 * (kayıt açma → modül servisi, eki kaydetme → DocumentService, bildirim → Graph sendMail / Teams).
 */
@Component
public class MailActionExecutor {

    private static final Logger log = LoggerFactory.getLogger(MailActionExecutor.class);

    public String execute(MailRule rule, MailMessage mail) {
        String result = switch (rule.getActionType()) {
            case KAYIT_AC -> rule.getTargetModule().getLabel() + " modülünde taslak kayıt açıldı";
            case EKI_KLASORE_KAYDET -> mail.hasAttachments()
                    ? "Ekler '" + rule.getTargetFolder() + "' klasörüne kaydedildi"
                    : "Ek bulunamadı, sadece kayıt düşüldü";
            case KISIYE_BILDIR -> rule.getNotifyEmail() + " kişisine bildirim gönderildi";
            case GOREV_OLUSTUR -> "Görev oluşturuldu: " + mail.subject();
        };
        log.info("Mail aksiyonu [{}] {} -> {}", rule.getName(), mail.subject(), result);
        return result;
    }
}
