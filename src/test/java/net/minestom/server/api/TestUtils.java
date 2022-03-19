package net.minestom.server.api;

import java.lang.ref.WeakReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtils {
    public static void waitUntilCleared(WeakReference<?> ref) {
        while (ref.get() != null) {
            System.gc();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignore) {
            }
        }
    }

    public static void assertEqualsIgnoreSpace(String s1, String s2, boolean matchCase) {
        final String val1 = stripExtraSpaces(s1);
        final String val2 = stripExtraSpaces(s2);
        if (matchCase) {
            assertEquals(val1, val2);
        } else {
            assertTrue(val1.equalsIgnoreCase(val2));
        }
    }

    public static void assertEqualsIgnoreSpace(String s1, String s2) {
        assertEqualsIgnoreSpace(s1, s2, true);
    }

    private static String stripExtraSpaces(String s) {
        StringBuilder formattedString = new StringBuilder();
        java.util.StringTokenizer st = new java.util.StringTokenizer(s);
        while (st.hasMoreTokens()) {
            formattedString.append(st.nextToken());
        }
        return formattedString.toString().trim();
    }
}
