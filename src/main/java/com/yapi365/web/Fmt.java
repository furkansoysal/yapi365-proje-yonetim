package com.yapi365.web;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/** Şablonlarda ${@fmt...} ile kullanılan Türkçe biçimlendiriciler. */
@Component("fmt")
public class Fmt {

    private static final Locale TR = Locale.forLanguageTag("tr-TR");
    private static final ZoneId IST = ZoneId.of("Europe/Istanbul");
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd.MM.yyyy", TR);
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", TR);

    public String d(LocalDate date) {
        return date == null ? "-" : D.format(date);
    }

    public String dt(Instant instant) {
        return instant == null ? "-" : DT.format(instant.atZone(IST));
    }

    public String money(BigDecimal amount) {
        if (amount == null) {
            return "-";
        }
        NumberFormat nf = NumberFormat.getNumberInstance(TR);
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(0);
        return nf.format(amount);
    }

    public String size(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format(TR, "%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        return mb < 1024 ? String.format(TR, "%.1f MB", mb) : String.format(TR, "%.2f GB", mb / 1024.0);
    }

    public long daysLeft(LocalDate date) {
        return date == null ? Long.MAX_VALUE : ChronoUnit.DAYS.between(LocalDate.now(IST), date);
    }

    public String any(Object o) {
        if (o == null) {
            return "—";
        }
        return o instanceof LocalDate ld ? d(ld) : o.toString();
    }
}
