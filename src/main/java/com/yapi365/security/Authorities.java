package com.yapi365.security;

import com.yapi365.domain.AppRole;
import com.yapi365.domain.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Kullanıcının rollerinden Spring Security yetkilerini üretir.
 * Örn. ROLE_PROJE_MUDURU, SOZLESME:VIEW, HAKEDIS:APPROVE
 * Controller'larda @PreAuthorize("hasAuthority('SOZLESME:EDIT')") şeklinde kullanılır.
 */
public final class Authorities {

    private Authorities() {
    }

    public static Set<GrantedAuthority> of(AppUser user) {
        Set<GrantedAuthority> result = new LinkedHashSet<>();
        for (AppRole role : user.getRoles()) {
            result.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            role.getPermissions().forEach(p -> result.add(new SimpleGrantedAuthority(p)));
        }
        return result;
    }
}
