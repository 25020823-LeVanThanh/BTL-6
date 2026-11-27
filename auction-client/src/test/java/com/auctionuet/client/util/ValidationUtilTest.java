package com.auctionuet.client.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationUtilTest {

    @Test
    void acceptsUsernameEmailAndMatchingPassword() {
        ValidationUtil.validateUsername("bidder_01");
        ValidationUtil.validateEmail("user.name@uet.edu.vn");
        ValidationUtil.validatePasswordConfirm("matkhau1", "matkhau1");
    }

    @Test
    void rejectsShortUsernameAndInvalidCharacters() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateUsername("ab"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateUsername("ten co dau cach"));
    }

    @Test
    void rejectsBlankAndMalformedEmail() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateEmail("  "));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateEmail("khong-phai-email"));
    }

    @Test
    void rejectsShortPasswordAndMismatch() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validatePassword("1234567"));
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validatePasswordConfirm("matkhau1", "matkhau2"));
    }

    @Test
    void parsesPortInsideRange() {
        assertEquals(3307, ValidationUtil.parsePort(" 3307 "));
    }

    @Test
    void rejectsPortOutsideRangeOrNotANumber() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parsePort("0"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parsePort("65536"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parsePort("abc"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parsePort(" "));
    }

    @Test
    void parsesAmountAndStripsThousandsSeparator() {
        assertEquals(1500000.5, ValidationUtil.parseAmount("1,500,000.5"));
    }

    @Test
    void rejectsEmptyZeroAndNonNumericAmount() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parseAmount(null));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parseAmount("0"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parseAmount("12a"));
    }
}
