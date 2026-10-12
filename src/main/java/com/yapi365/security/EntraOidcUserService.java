package com.yapi365.security;

import com.yapi365.domain.AppUser;
import com.yapi365.repo.AppUserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Microsoft Entra ID (Microsoft 365) ile giriş yapan kullanıcıyı sistemdeki kullanıcıyla eşleştirir.
 * Yetkiler Entra'dan değil uygulama içindeki rol matrisinden gelir; böylece yetkiler arayüzden yönetilir.
 */
public class EntraOidcUserService extends OidcUserService {

    private final AppUserRepository users;

    public EntraOidcUserService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        OidcUser oidc = super.loadUser(request);
        String email = oidc.getPreferredUsername() != null ? oidc.getPreferredUsername() : oidc.getEmail();
        if (email == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user", "E-posta bilgisi alınamadı", null));
        }
        AppUser user = users.findByEmailIgnoreCase(email)
                .filter(AppUser::isActive)
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("access_denied", "Bu kullanıcı sistemde tanımlı değil: " + email, null)));
        Set<GrantedAuthority> authorities = Authorities.of(user);
        return new DefaultOidcUser(authorities, oidc.getIdToken(), oidc.getUserInfo(), "preferred_username");
    }
}
