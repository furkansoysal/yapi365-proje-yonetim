package com.yapi365.security;

import com.yapi365.domain.AppUser;
import com.yapi365.repo.AppUserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Demo / yerel geliştirme girişi. Canlıda "entra" profili ile yalnızca Microsoft 365 SSO kullanılır. */
@Service
@Profile("!entra")
public class DbUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    public DbUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        AppUser u = users.findByEmailIgnoreCase(username)
                .filter(AppUser::isActive)
                .filter(x -> x.getPasswordHash() != null)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return User.withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .authorities(Authorities.of(u))
                .build();
    }
}
