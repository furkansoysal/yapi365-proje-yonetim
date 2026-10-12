package com.yapi365.domain;

public enum SyncStatus {
    PENDING("Kuyrukta"),
    SYNCED("SharePoint'e aktarıldı"),
    FAILED("Hata - tekrar denenecek"),
    DISABLED("Senkron kapalı");

    private final String label;

    SyncStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
