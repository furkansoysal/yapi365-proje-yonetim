package com.yapi365.config;

import com.yapi365.domain.*;
import com.yapi365.repo.*;
import com.yapi365.service.DocumentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.yapi365.domain.Action.*;
import static com.yapi365.domain.AppModule.*;

/**
 * İlk açılışta 10 rolü ve yetki matrisini oluşturur.
 * app.demo-data=true ise örnek proje, kullanıcı, sözleşme ve mail kuralları da eklenir.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final AppRoleRepository roles;
    private final AppUserRepository users;
    private final ProjectRepository projects;
    private final ContractRepository contracts;
    private final MailRuleRepository mailRules;
    private final MailLogRepository mailLogs;
    private final DocumentService documents;
    private final PasswordEncoder encoder;
    private final TransactionTemplate tx;
    private final boolean demoData;

    public DataSeeder(AppRoleRepository roles, AppUserRepository users, ProjectRepository projects,
                      ContractRepository contracts, MailRuleRepository mailRules, MailLogRepository mailLogs,
                      DocumentService documents, PasswordEncoder encoder, TransactionTemplate tx,
                      @Value("${app.demo-data:false}") boolean demoData) {
        this.roles = roles;
        this.users = users;
        this.projects = projects;
        this.contracts = contracts;
        this.mailRules = mailRules;
        this.mailLogs = mailLogs;
        this.documents = documents;
        this.encoder = encoder;
        this.tx = tx;
        this.demoData = demoData;
    }

    @Override
    public void run(String... args) {
        tx.executeWithoutResult(s -> {
            if (roles.count() == 0) {
                seedRoles();
            }
        });
        if (demoData && users.count() == 0) {
            tx.executeWithoutResult(s -> seedDemo());
        }
    }

    private void seedRoles() {
        AppRole admin = new AppRole("SISTEM_YONETICISI", "Sistem Yöneticisi");
        for (AppModule m : AppModule.values()) {
            admin.grant(m, VIEW, EDIT, APPROVE);
        }

        AppRole pm = new AppRole("PROJE_MUDURU", "Proje Müdürü");
        for (AppModule m : AppModule.values()) {
            if (m != YONETIM) {
                pm.grant(m, VIEW, EDIT, APPROVE);
            }
        }

        AppRole site = new AppRole("SANTIYE_SEFI", "Şantiye Şefi");
        site.grant(PROJE, VIEW);
        site.grant(DOKUMAN, VIEW, EDIT);
        site.grant(SOZLESME, VIEW);
        site.grant(HAKEDIS, VIEW, EDIT);
        site.grant(IS_PROGRAMI, VIEW, EDIT);
        site.grant(PUANTAJ, VIEW, EDIT, APPROVE);
        site.grant(EKIPMAN, VIEW, EDIT);
        site.grant(ISG, VIEW, EDIT);
        site.grant(CLAIM, VIEW, EDIT);

        AppRole office = new AppRole("TEKNIK_OFIS", "Teknik Ofis");
        office.grant(PROJE, VIEW);
        office.grant(DOKUMAN, VIEW, EDIT);
        office.grant(SOZLESME, VIEW, EDIT);
        office.grant(HAKEDIS, VIEW, EDIT);
        office.grant(IS_PROGRAMI, VIEW, EDIT);
        office.grant(CLAIM, VIEW, EDIT);

        AppRole progress = new AppRole("HAKEDIS_SORUMLUSU", "Hakediş Sorumlusu");
        progress.grant(PROJE, VIEW);
        progress.grant(DOKUMAN, VIEW);
        progress.grant(SOZLESME, VIEW);
        progress.grant(HAKEDIS, VIEW, EDIT, APPROVE);
        progress.grant(FATURA, VIEW);

        AppRole finance = new AppRole("MUHASEBE", "Muhasebe / Finans");
        finance.grant(PROJE, VIEW);
        finance.grant(SOZLESME, VIEW);
        finance.grant(HAKEDIS, VIEW, APPROVE);
        finance.grant(FATURA, VIEW, EDIT, APPROVE);
        finance.grant(DOKUMAN, VIEW);

        AppRole purchase = new AppRole("SATINALMA", "Satınalma");
        purchase.grant(PROJE, VIEW);
        purchase.grant(SOZLESME, VIEW, EDIT);
        purchase.grant(DOKUMAN, VIEW, EDIT);
        purchase.grant(EKIPMAN, VIEW, EDIT);
        purchase.grant(FATURA, VIEW);

        AppRole hse = new AppRole("ISG_UZMANI", "İSG Uzmanı");
        hse.grant(PROJE, VIEW);
        hse.grant(ISG, VIEW, EDIT, APPROVE);
        hse.grant(PUANTAJ, VIEW);
        hse.grant(DOKUMAN, VIEW, EDIT);

        AppRole hr = new AppRole("IK_UZMANI", "İK Uzmanı");
        hr.grant(PROJE, VIEW);
        hr.grant(IK, VIEW, EDIT, APPROVE);
        hr.grant(PUANTAJ, VIEW, EDIT);

        AppRole viewer = new AppRole("IZLEYICI", "İzleyici (Yalnız Okuma)");
        for (AppModule m : List.of(PROJE, DOKUMAN, SOZLESME, HAKEDIS, IS_PROGRAMI)) {
            viewer.grant(m, VIEW);
        }

        roles.saveAll(List.of(admin, pm, site, office, progress, finance, purchase, hse, hr, viewer));
    }

    private void seedDemo() {
        Project p1 = projects.save(new Project("PRJ-2026-01", "Kartal Konut Projesi - A Blok"));
        Project p2 = projects.save(new Project("PRJ-2026-02", "Gebze Lojistik Depo"));

        user("yonetici@demo.local", "Sistem Yöneticisi", "SISTEM_YONETICISI", p1, p2);
        user("proje.muduru@demo.local", "Proje Müdürü", "PROJE_MUDURU", p1, p2);
        user("santiye.sefi@demo.local", "Şantiye Şefi", "SANTIYE_SEFI", p1);
        user("hakedis@demo.local", "Hakediş Sorumlusu", "HAKEDIS_SORUMLUSU", p1, p2);
        user("muhasebe@demo.local", "Muhasebe", "MUHASEBE", p1, p2);
        user("izleyici@demo.local", "İzleyici", "IZLEYICI", p2);

        LocalDate today = LocalDate.now();
        contracts.save(contract(p1, ContractType.MUSTERI, "SZL-001", "Kartal Yapı Kooperatifi",
                "A Blok anahtar teslim inşaat işleri", today.minusMonths(8), today.plusMonths(10),
                "48500000", "2425000", today.plusDays(45)));
        contracts.save(contract(p1, ContractType.ALT_YUKLENICI, "SZL-014", "Demir Kalıp Ltd.",
                "Kaba inşaat kalıp ve demir işleri", today.minusMonths(6), today.plusDays(28),
                "6750000", "337500", today.plusDays(20)));
        contracts.save(contract(p1, ContractType.TEDARIK, "SZL-021", "Marmara Hazır Beton A.Ş.",
                "C30 hazır beton tedariki", today.minusMonths(5), today.plusMonths(4),
                "3900000", null, null));
        contracts.save(contract(p1, ContractType.DIS_HIZMET, "SZL-030", "Güven İSG Hizmetleri",
                "OSGB iş güvenliği uzmanı ve işyeri hekimi hizmeti", today.minusMonths(3), today.plusDays(55),
                "420000", null, null));
        contracts.save(contract(p2, ContractType.MUSTERI, "SZL-101", "Gebze Lojistik A.Ş.",
                "Çelik konstrüksiyon depo yapımı", today.minusMonths(2), today.plusMonths(12),
                "31200000", "1560000", today.plusMonths(13)));

        doc(p1, "Sözleşmeler/Müşteri", "SZL-001 Ana Sözleşme", "SZL-001_ana_sozlesme.pdf");
        doc(p1, "Projeler/Statik", "A Blok Kalıp Planı", "A-blok-kalip-plani-rev1.dwg");
        doc(p1, "Projeler/Statik", "A Blok Kalıp Planı", "A-blok-kalip-plani-rev2.dwg");
        doc(p1, "Hakedişler/2026", "Hakediş No 7", "hakedis-07.xlsx");
        doc(p2, "Sözleşmeler/Müşteri", "SZL-101 Ana Sözleşme", "SZL-101_sozlesme.pdf");

        mailRules.save(rule("Hakediş mailleri", 10, null, "hakediş", null,
                MailActionType.KAYIT_AC, AppModule.HAKEDIS, null, null));
        mailRules.save(rule("Fatura ekleri", 20, null, null, "fatura, e-fatura, irsaliye",
                MailActionType.EKI_KLASORE_KAYDET, AppModule.FATURA, "Faturalar/Gelen", null));
        mailRules.save(rule("Satınalma talepleri", 30, null, "satın alma", "teklif, sipariş",
                MailActionType.KISIYE_BILDIR, AppModule.SOZLESME, null, "satinalma@demo.local"));
        mailRules.save(rule("İSG bildirimleri", 40, "osgb", null, "kaza, ramak kala, eğitim",
                MailActionType.GOREV_OLUSTUR, AppModule.ISG, null, null));

        Instant now = Instant.now();
        mailLogs.save(new MailLog("demo-1", now.minus(35, ChronoUnit.MINUTES), "muhasebe@demirkalip.com",
                "Ekim ayı hakediş no 7 onaya sunulmuştur", "Hakediş mailleri", "KURAL",
                "İlerleme Bazlı Faturalama modülünde taslak kayıt açıldı"));
        mailLogs.save(new MailLog("demo-2", now.minus(2, ChronoUnit.HOURS), "efatura@marmarabeton.com.tr",
                "MRB2026000412 numaralı e-fatura", "Fatura ekleri", "KURAL",
                "Ekler 'Faturalar/Gelen' klasörüne kaydedildi"));
        mailLogs.save(new MailLog("demo-3", now.minus(5, ChronoUnit.HOURS), "info@tedarikci.com",
                "Toplantı hakkında", null, "LLM", "Sınıflandırılamadı - manuel inceleme"));
    }

    private void user(String email, String name, String roleCode, Project... access) {
        AppUser u = new AppUser(email, name);
        u.setPasswordHash(encoder.encode("demo"));
        u.getRoles().add(roles.findByCode(roleCode).orElseThrow());
        u.getProjects().addAll(List.of(access));
        users.save(u);
    }

    private static Contract contract(Project p, ContractType type, String no, String party, String title,
                                     LocalDate start, LocalDate end, String amount, String guarantee,
                                     LocalDate guaranteeExpiry) {
        Contract c = new Contract();
        c.setProject(p);
        c.setType(type);
        c.setContractNo(no);
        c.setCounterparty(party);
        c.setTitle(title);
        c.setStartDate(start);
        c.setEndDate(end);
        c.setAmount(new BigDecimal(amount));
        c.setGuaranteeAmount(guarantee == null ? null : new BigDecimal(guarantee));
        c.setGuaranteeExpiry(guaranteeExpiry);
        return c;
    }

    private void doc(Project p, String folder, String title, String fileName) {
        try {
            byte[] content = ("Örnek içerik: " + fileName).getBytes(StandardCharsets.UTF_8);
            documents.upload(p, folder, title, fileName, "application/octet-stream",
                    new ByteArrayInputStream(content), "sistem");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static MailRule rule(String name, int priority, String sender, String subject, String keywords,
                                 MailActionType action, AppModule module, String folder, String notify) {
        MailRule r = new MailRule();
        r.setName(name);
        r.setPriority(priority);
        r.setSenderContains(sender);
        r.setSubjectContains(subject);
        r.setKeywords(keywords);
        r.setActionType(action);
        r.setTargetModule(module);
        r.setTargetFolder(folder);
        r.setNotifyEmail(notify);
        return r;
    }
}
