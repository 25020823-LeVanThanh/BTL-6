package com.auctionuet.client.util;

/**
 * Kiểm tra dữ liệu nhập trên form đăng ký và các ô số tiền.
 */
public final class ValidationUtil {
    private static final String EMAIL_PATTERN = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";

    private ValidationUtil() {
    }

    public static void validateUsername(String username) {
        if (username == null || username.isBlank() || username.trim().length() < 3) {
            throw new IllegalArgumentException("Tên đăng nhập phải có ít nhất 3 ký tự.");
        }
        if (!username.trim().matches("^\\w+$")) {
            throw new IllegalArgumentException("Tên đăng nhập chỉ được chứa chữ, số và dấu gạch dưới.");
        }
    }

    public static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email không được để trống.");
        }
        if (!email.trim().matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự.");
        }
    }

    public static void validatePasswordConfirm(String password, String confirm) {
        validatePassword(password);
        if (confirm == null || !password.equals(confirm)) {
            throw new IllegalArgumentException("Mật khẩu nhập lại không khớp.");
        }
    }

    public static int parsePort(String rawPort) {
        if (rawPort == null || rawPort.isBlank()) {
            throw new IllegalArgumentException("Port phải là số.");
        }
        try {
            int port = Integer.parseInt(rawPort.trim());
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Port phải từ 1 đến 65535.");
            }
            return port;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Port phải là số.");
        }
    }

    public static double parseAmount(String rawAmount) {
        if (rawAmount == null || rawAmount.isBlank()) {
            throw new IllegalArgumentException("Số tiền không được để trống.");
        }
        String digits = rawAmount.replace(",", "").trim();
        try {
            double amount = Double.parseDouble(digits);
            if (amount <= 0) {
                throw new IllegalArgumentException("Số tiền phải lớn hơn 0.");
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Số tiền không hợp lệ.");
        }
    }
}
