package com.brightpessimist.imex.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public final class BigNumberFormat {

    // irregular stems: million, billion, trillion, ... nonillion
    private static final String[] SMALL = {
        "", "m", "b", "tr", "quadr", "quint", "sext", "sept", "oct", "non"
    };
    // Latin components (Conway–Wechsler style)
    private static final String[] UNITS = {
        "", "un", "duo", "tre", "quattuor", "quin", "sex", "septen", "octo", "novem"
    };
    private static final String[] TENS = {
        "", "deci", "viginti", "triginta", "quadraginta", "quinquaginta",
        "sexaginta", "septuaginta", "octoginta", "nonaginta"
    };
    private static final String[] HUNDREDS = {
        "", "centi", "ducenti", "trecenti", "quadringenti", "quingenti",
        "sescenti", "septingenti", "octingenti", "nongenti"
    };

    private static final int MAX_N = 999;   // names up to 10^3000 → 3000-digit numbers

    private BigNumberFormat() {}

    public static String format(BigInteger value) { return format(value, 3); }

    public static String format(BigInteger value, int decimals) {
        if (value.signum() == 0) return "0";
        String sign = value.signum() < 0 ? "-" : "";

        BigDecimal bd = new BigDecimal(value.abs());
        int digits = bd.precision();       // cheap digit count
        int group  = (digits - 1) / 3;     // 1 = thousand, 2 = million, ...

        if (group == 0) return sign + bd.toPlainString();   // under 1000: show exactly

        int n = group - 1;                 // 1 = million, 100 = centillion, ...
        if (n > MAX_N) {                   // scientific fallback (what the game does too)
            BigDecimal m = bd.movePointLeft(digits - 1).setScale(decimals, RoundingMode.DOWN);
            return sign + m.toPlainString() + "e" + (digits - 1);
        }
        BigDecimal m = bd.movePointLeft(3 * group).setScale(decimals, RoundingMode.DOWN);
        return sign + m.toPlainString() + " " + scaleName(group);
    }

    /** name of 10^(3*group) */
    private static String scaleName(int group) {
        if (group == 1) return "thousand";
        int n = group - 1;
        if (n < 10) return SMALL[n] + "illion";
        String stem = UNITS[n % 10] + TENS[(n / 10) % 10] + HUNDREDS[n / 100];
        // joining vowel drops before "illion": viginti + illion -> vigintillion
        return stem.substring(0, stem.length() - 1) + "illion";
    }
}
