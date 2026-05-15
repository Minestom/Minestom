package net.minestom.demo.core;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Lazy loader for the {@code minestom.png} favicon embedded in
 * {@code demo/src/main/resources}. Used by
 * {@link ServerListPingFeature}.
 */
public final class Favicon {

    private static final String RESOURCE = "/minestom.png";

    private static volatile byte @Nullable [] cached;

    private Favicon() {
    }

    public static byte[] bytes() {
        byte[] local = cached;
        if (local != null) return local;
        synchronized (Favicon.class) {
            if (cached != null) return cached;
            try (InputStream stream = Favicon.class.getResourceAsStream(RESOURCE)) {
                cached = Objects.requireNonNull(stream, "missing " + RESOURCE).readAllBytes();
                return cached;
            } catch (IOException e) {
                throw new RuntimeException("failed to load " + RESOURCE, e);
            }
        }
    }
}
