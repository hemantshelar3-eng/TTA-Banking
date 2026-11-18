package com.tta.bank.utils;

import java.math.BigDecimal;

public final class MoneyUtil {

    private MoneyUtil() { }

    /** "₹51,000" -> 51000 ; "-₹1,000" -> -1000 ; "+₹20,000" -> 20000 ; "₹799.8" -> 799.8 */
    public static BigDecimal parse(String text) {
        String cleaned = text == null ? "" : text.replaceAll("[^0-9.\\-]", "");
        if (cleaned.isEmpty() || cleaned.equals("-") || cleaned.equals(".")) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(cleaned);
    }

    public static BigDecimal of(String value) {
        return new BigDecimal(value);
    }
}
