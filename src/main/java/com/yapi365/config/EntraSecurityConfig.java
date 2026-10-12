package com.yapi365.config;

import com.yapi365.repo.AppUserRepository;
import com.yapi365.security.EntraOidcUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Canlı ortam: Microsoft 365 hesabı ile tek oturum (SSO).
 * application-entra.yml içindeki Entra uygulama kaydı bilgileri ile çalışır.
 */
@Configuration
@Profile("entra")
public class EntraSecurityConfig {

    @Bean
    SecurityFilterChain entraFilterChain(HttpSecurity http, AppUserRepository users) throws Exception {
        http
            .authorizeHttpRequests(a -> a
                .requestMatchers("/css/**", "/error", "/actuator/health").permitAll()
                .anyRequest().authenticated())
            .oauth2Login(o -> o
                .userInfoEndpoint(u -> u.oidcUserService(new EntraOidcUserService(users)))
                .defaultSuccessUrl("/", true))
            .logout(l -> l.logoutSuccessUrl("/"));
        return http.build();
    }
}
