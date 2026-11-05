package com.auctionuet.client.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Định dạng thời gian giống các nhãn trên giao diện.
 */
public final class DateTimeUtil {
    public static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter DISPLAY_WITH_SECONDS = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private DateTimeUtil() {
    }

    public static String format(LocalDateTime time) {
        return time == null ? "--" : time.format(DISPLAY);
    }

    public static String formatWithSeconds(LocalDateTime time) {
        return time == null ? "--" : time.format(DISPLAY_WITH_SECONDS);
    }
}
