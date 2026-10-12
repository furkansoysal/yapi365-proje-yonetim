package com.yapi365.graph;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Microsoft Graph uygulama kaydı (Entra ID → App registrations) bilgileri.
 * Gerekli uygulama izinleri: Files.ReadWrite.All (veya Sites.Selected), Mail.ReadWrite (paylaşılan kutu için).
 */
@ConfigurationProperties("app.graph")
public record GraphProperties(
        boolean enabled,
        String tenantId,
        String clientId,
        String clientSecret,
        String driveId,
        String rootFolder,
        String sharedMailbox) {
}
