package com.auctionuet.client.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateTimeUtilTest {

    @Test
    void formatsDisplayPatterns() {
        LocalDateTime time = LocalDateTime.of(2026, 11, 27, 16, 5, 9);

        assertEquals("27/11/2026 16:05", DateTimeUtil.format(time));
        assertEquals("27/11/2026 16:05:09", DateTimeUtil.formatWithSeconds(time));
    }

    @Test
    void missingTimeRendersPlaceholder() {
        assertEquals("--", DateTimeUtil.format(null));
        assertEquals("--", DateTimeUtil.formatWithSeconds(null));
    }
}
