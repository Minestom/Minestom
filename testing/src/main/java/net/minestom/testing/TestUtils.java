package net.minestom.testing;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.coordinate.Point;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public final class TestUtils {
    public static void waitUntilCleared(WeakReference<?> ref) {
        final int maxTries = 100;

        for (int i = 0; i < maxTries; i++) {
            System.gc();
            if (ref.get() == null) {
                return;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        fail("Reference was not cleared");
    }

    public static <T> void assertEqualsIgnoreOrder(Collection<T> expected, Collection<? extends T> actual) {
        assertEquals(Set.copyOf(expected), Set.copyOf(actual));
    }

    public static void assertEqualsSNBT(String snbt, BinaryTag compound) {
        try {
            final var converted = MinestomAdventure.tagStringIO().asTag(snbt);
            assertEquals(converted, compound);
        } catch (IOException e) {
            fail(e);
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

    public static void assertPoint(Point p1, Point p2) {
        assertTrue(p1.samePoint(p2), String.format("Points don't match! Expected: %s, but got: %s", p1, p2));
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
