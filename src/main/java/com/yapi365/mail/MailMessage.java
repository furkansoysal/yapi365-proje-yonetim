package com.yapi365.mail;

import java.time.Instant;

public record MailMessage(String id, String sender, String subject, String body,
                          Instant receivedAt, boolean hasAttachments) {
}
