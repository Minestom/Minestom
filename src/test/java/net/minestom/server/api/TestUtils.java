package net.minestom.server.api;

import java.lang.ref.WeakReference;

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
}
