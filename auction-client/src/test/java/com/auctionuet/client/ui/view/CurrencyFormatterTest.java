package com.auctionuet.client.ui.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyFormatterTest {

    @Test
    void showsFullAmountUpToOneHundredBillion() {
        assertEquals("1,500,000 VND", CurrencyFormatter.format(1_500_000));
        assertEquals("100,000,000,000 VND", CurrencyFormatter.format(100_000_000_000d));
    }

    @Test
    void compactsAmountsAboveOneHundredBillion() {
        assertEquals("200 tỷ VND", CurrencyFormatter.format(200_000_000_000d));
        assertEquals("1.5 nghìn tỷ VND", CurrencyFormatter.format(1_500_000_000_000d));
        assertEquals("2 triệu tỷ VND", CurrencyFormatter.format(2_000_000_000_000_000d));
    }

    @Test
    void fullFormatKeepsExactAmount() {
        assertEquals("200,000,000,000 VND", CurrencyFormatter.formatFull(200_000_000_000d));
    }

    @Test
    void nonFiniteAmountUsesPlaceholder() {
        assertEquals("-- VND", CurrencyFormatter.format(Double.NaN));
        assertEquals("-- VND", CurrencyFormatter.formatFull(Double.POSITIVE_INFINITY));
    }
}
