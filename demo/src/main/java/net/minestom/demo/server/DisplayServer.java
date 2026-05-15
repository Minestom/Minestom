package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.feature.display.DisplayFeature;

/**
 * Display-entity showcase: only the {@code /display} command and the
 * default lobby — minimal scaffolding for working on display rendering.
 */
public final class DisplayServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new DisplayFeature()
        ).start();
    }
}
