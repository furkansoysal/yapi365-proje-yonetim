package com.yapi365.graph;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

/** Client credentials akışı ile Graph erişim anahtarı alır ve süresi dolana kadar önbellekte tutar. */
@Component
public class GraphTokenProvider {

    private final GraphProperties props;
    private final RestClient http = RestClient.create();

    private String token;
    private Instant expiresAt = Instant.EPOCH;

    public GraphTokenProvider(GraphProperties props) {
        this.props = props;
    }

    public synchronized String token() {
        if (token != null && Instant.now().isBefore(expiresAt.minusSeconds(120))) {
            return token;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", props.clientId());
        form.add("client_secret", props.clientSecret());
        form.add("scope", "https://graph.microsoft.com/.default");

        @SuppressWarnings("unchecked")
        Map<String, Object> res = http.post()
                .uri("https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token", props.tenantId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        if (res == null || res.get("access_token") == null) {
            throw new IllegalStateException("Graph token alınamadı");
        }
        token = (String) res.get("access_token");
        long expiresIn = ((Number) res.getOrDefault("expires_in", 3600)).longValue();
        expiresAt = Instant.now().plusSeconds(expiresIn);
        return token;
    }
}
