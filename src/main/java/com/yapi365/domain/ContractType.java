package com.yapi365.domain;

public enum ContractType {
    MUSTERI("Müşteri"),
    ALT_YUKLENICI("Alt Yüklenici"),
    TEDARIK("Tedarik"),
    DIS_HIZMET("Dış Hizmet");

    private final String label;

    ContractType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
