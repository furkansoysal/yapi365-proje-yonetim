package com.yapi365.domain;

public enum MailActionType {
    KAYIT_AC("İlgili modülde kayıt aç"),
    EKI_KLASORE_KAYDET("Eki ilgili klasöre kaydet"),
    KISIYE_BILDIR("İlgili kişiye bildir"),
    GOREV_OLUSTUR("Görev oluştur");

    private final String label;

    MailActionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
