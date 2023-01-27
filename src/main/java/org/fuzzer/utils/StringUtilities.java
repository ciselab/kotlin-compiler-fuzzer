package org.fuzzer.utils;

import com.mifmif.common.regex.Generex;

public class StringUtilities {

    public static String correctEscapedCharacter(String str) {
        return switch (str) {
            case "\\n" -> "\n";
            case "\\r" -> "\r";
            default -> str;
        };
    }

    public static String removeEscapedCharacters(String str) {
        return str.replace("\\n", "\n").replace("\\r", "\r");
    }

    public static String sampleRegex(String regex) {
        return new Generex(regex).random();
    }

    public static String randomIdentifier() {
        return sampleRegex("[a-zA-Z][a-zA-Z0-9_]*");
    }

    public static String randomString() {
        return "\"" + sampleRegex("[a-zA-Z][a-zA-Z0-9]*") + "\"";
    }

    public static String randomChar() {
        return "'" + sampleRegex("[a-zA-Z0-9]") + "'";
    }
}
