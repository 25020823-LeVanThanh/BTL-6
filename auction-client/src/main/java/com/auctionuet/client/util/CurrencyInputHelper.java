package com.auctionuet.client.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Tự chèn dấu phẩy hàng nghìn khi nhập số tiền.
 */
public final class CurrencyInputHelper {
    private CurrencyInputHelper() {
    }

    public static void setupCurrencyInput(TextField textField) {
        if (textField == null) {
            return;
        }

        textField.textProperty().addListener(new ChangeListener<String>() {
            private boolean updating;

            @Override
            public void changed(ObservableValue<? extends String> obs, String oldValue, String newValue) {
                if (updating) {
                    return;
                }

                String clean = newValue == null ? "" : newValue.replaceAll("[^\\d]", "");
                if (clean.isEmpty()) {
                    updating = true;
                    textField.setText("");
                    updating = false;
                    return;
                }

                try {
                    if (clean.length() > 15) {
                        clean = clean.substring(0, 15);
                    }
                    long value = Long.parseLong(clean);
                    DecimalFormat format = new DecimalFormat("#,##0");
                    format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
                    String formatted = format.format(value);

                    int caret = textField.getCaretPosition();
                    String current = textField.getText() == null ? "" : textField.getText();
                    int digitsBeforeCaret = 0;
                    for (int i = 0; i < Math.min(caret, current.length()); i++) {
                        if (Character.isDigit(current.charAt(i))) {
                            digitsBeforeCaret++;
                        }
                    }

                    updating = true;
                    textField.setText(formatted);
                    int newCaret = formatted.length();
                    int digitCount = 0;
                    for (int i = 0; i < formatted.length(); i++) {
                        if (Character.isDigit(formatted.charAt(i))) {
                            digitCount++;
                        }
                        if (digitCount == digitsBeforeCaret) {
                            newCaret = i + 1;
                            break;
                        }
                    }
                    textField.selectRange(newCaret, newCaret);
                    updating = false;
                } catch (NumberFormatException ex) {
                    updating = true;
                    textField.setText(oldValue);
                    updating = false;
                }
            }
        });
    }
}
