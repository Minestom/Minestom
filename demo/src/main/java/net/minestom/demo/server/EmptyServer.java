package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;

/**
 * Smallest possible reproducer: just the {@link LobbyFeature} default
 * flat world plus offline auth. Useful for bisecting whether a bug is in
 * the framework itself or in a specific demo feature.
 */
public final class EmptyServer {

    static void main(String[] args) {
        DemoServer.create()
                .feature(new LobbyFeature())
                .start();
    }
}
