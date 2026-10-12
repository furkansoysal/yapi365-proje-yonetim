package com.yapi365.domain;

/** Sistemdeki modüller. Yetkiler "MODUL:AKSIYON" formatında tutulur (örn. SOZLESME:APPROVE). */
public enum AppModule {
    PROJE("Projeler"),
    DOKUMAN("Doküman Yönetimi"),
    SOZLESME("Sözleşme Yönetimi"),
    HAKEDIS("İlerleme Bazlı Faturalama"),
    FATURA("Fatura ve Ödeme Takibi"),
    CLAIM("Talep / Hak İddiası"),
    IS_PROGRAMI("İş Programı"),
    PUANTAJ("Personel Puantajı"),
    EKIPMAN("Makine / Ekipman"),
    ISG("İş Sağlığı ve Güvenliği"),
    IK("İnsan Kaynakları"),
    MAIL("Mail Otomasyonu"),
    YONETIM("Sistem Yönetimi");

    private final String label;

    AppModule(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
