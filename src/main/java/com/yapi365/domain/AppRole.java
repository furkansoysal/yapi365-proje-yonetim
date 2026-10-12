package com.yapi365.domain;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "app_role")
public class AppRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permission", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission", length = 60)
    private Set<String> permissions = new LinkedHashSet<>();

    protected AppRole() {
    }

    public AppRole(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public boolean has(AppModule module, Action action) {
        return permissions.contains(Action.permission(module, action));
    }

    public void grant(AppModule module, Action... actions) {
        for (Action a : actions) {
            permissions.add(Action.permission(module, a));
        }
    }

    public void toggle(String permission) {
        if (!permissions.remove(permission)) {
            permissions.add(permission);
        }
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public Set<String> getPermissions() { return permissions; }
}
