package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.feature.display.DisplayFeature;

/** {@code /display} command only. */
public final class DisplayServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new DisplayFeature()
        ).start();
    }
}
