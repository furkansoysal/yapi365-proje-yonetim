package com.yapi365.domain;

public enum Action {
    VIEW("Görüntüle"),
    EDIT("Düzenle"),
    APPROVE("Onayla");

    private final String label;

    Action(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static String permission(AppModule module, Action action) {
        return module.name() + ":" + action.name();
    }
}
