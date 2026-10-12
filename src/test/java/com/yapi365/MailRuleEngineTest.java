package com.yapi365;

import com.yapi365.domain.MailActionType;
import com.yapi365.domain.MailRule;
import com.yapi365.mail.MailMessage;
import com.yapi365.mail.MailRuleEngine;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MailRuleEngineTest {

    private final MailRuleEngine engine = new MailRuleEngine();

    private static MailRule rule(String name, int prio, String sender, String subject, String keywords) {
        MailRule r = new MailRule();
        r.setName(name);
        r.setPriority(prio);
        r.setSenderContains(sender);
        r.setSubjectContains(subject);
        r.setKeywords(keywords);
        r.setActionType(MailActionType.GOREV_OLUSTUR);
        return r;
    }

    private static MailMessage mail(String sender, String subject, String body) {
        return new MailMessage("1", sender, subject, body, Instant.now(), false);
    }

    @Test
    void turkceBuyukKucukHarfDuyarsizEslesir() {
        var rules = List.of(rule("Hakediş", 10, null, "hakediş", null));
        assertThat(engine.match(rules, mail("a@b.com", "EKİM HAKEDİŞ RAPORU", ""))).isPresent();
    }

    @Test
    void oncelikDusukOlanOnceDenenir() {
        var genel = rule("Genel", 50, null, null, "fatura");
        var ozel = rule("Özel", 5, "beton", null, "fatura");
        var result = engine.match(List.of(genel, ozel), mail("efatura@marmarabeton.com", "Fatura", ""));
        assertThat(result).get().extracting(MailRule::getName).isEqualTo("Özel");
    }

    @Test
    void tumKosullarSaglanmalidir() {
        var r = rule("Satınalma", 10, "tedarikci", "teklif", null);
        assertThat(engine.match(List.of(r), mail("info@baska.com", "Teklif", ""))).isEmpty();
        assertThat(engine.match(List.of(r), mail("satis@tedarikci.com", "Fiyat teklifi", ""))).isPresent();
    }

    @Test
    void anahtarKelimeGovdedeDeAranir() {
        var r = rule("İSG", 10, null, null, "kaza, ramak kala");
        assertThat(engine.match(List.of(r), mail("x@y.com", "Bilgi", "Bugün ramak kala olayı yaşandı"))).isPresent();
    }

    @Test
    void pasifVeKosulsuzKurallarYokSayilir() {
        var pasif = rule("Pasif", 1, null, "hakediş", null);
        pasif.setActive(false);
        var bos = rule("Boş", 2, null, null, null);
        assertThat(engine.match(List.of(pasif, bos), mail("a@b.com", "hakediş", ""))).isEmpty();
    }
}
