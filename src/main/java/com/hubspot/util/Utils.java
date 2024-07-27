package com.hubspot.util;

import java.util.Map;

public final class Utils {

    // Private constructor to prevent instantiation
    private Utils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String getEnvCaseInsensitive(String key) {
        Map<String, String> env = System.getenv();
        for (Map.Entry<String, String> entry : env.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
