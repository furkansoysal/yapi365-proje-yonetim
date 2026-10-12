package com.yapi365.graph;

import com.yapi365.mail.MailMessage;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Paylaşılan posta kutusundaki okunmamış mailleri okur (uygulama izni: Mail.ReadWrite). */
@Component
public class GraphMailClient {

    private final GraphProperties props;
    private final GraphTokenProvider tokens;
    private final RestClient graph = RestClient.create();

    public GraphMailClient(GraphProperties props, GraphTokenProvider tokens) {
        this.props = props;
        this.tokens = tokens;
    }

    @SuppressWarnings("unchecked")
    public List<MailMessage> unread(int top) {
        URI uri = UriComponentsBuilder.fromUriString("https://graph.microsoft.com/v1.0")
                .pathSegment("users", props.sharedMailbox(), "mailFolders", "inbox", "messages")
                .queryParam("$filter", "isRead eq false")
                .queryParam("$top", top)
                .queryParam("$orderby", "receivedDateTime asc")
                .queryParam("$select", "id,subject,from,receivedDateTime,bodyPreview,hasAttachments")
                .encode()
                .build()
                .toUri();

        Map<String, Object> res = graph.get()
                .uri(uri)
                .header("Authorization", "Bearer " + tokens.token())
                .retrieve()
                .body(Map.class);

        List<MailMessage> list = new ArrayList<>();
        if (res == null) {
            return list;
        }
        for (Map<String, Object> m : (List<Map<String, Object>>) res.getOrDefault("value", List.of())) {
            Map<String, Object> from = (Map<String, Object>) m.get("from");
            Map<String, Object> addr = from == null ? Map.of() : (Map<String, Object>) from.get("emailAddress");
            list.add(new MailMessage(
                    (String) m.get("id"),
                    (String) addr.getOrDefault("address", ""),
                    (String) m.getOrDefault("subject", ""),
                    (String) m.getOrDefault("bodyPreview", ""),
                    Instant.parse((String) m.get("receivedDateTime")),
                    Boolean.TRUE.equals(m.get("hasAttachments"))));
        }
        return list;
    }

    public void markRead(String messageId) {
        URI uri = UriComponentsBuilder.fromUriString("https://graph.microsoft.com/v1.0")
                .pathSegment("users", props.sharedMailbox(), "messages", messageId)
                .encode().build().toUri();
        graph.patch()
                .uri(uri)
                .header("Authorization", "Bearer " + tokens.token())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("isRead", true))
                .retrieve()
                .toBodilessEntity();
    }
}
