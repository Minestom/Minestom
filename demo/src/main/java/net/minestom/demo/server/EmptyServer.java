package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;

/** Lobby only — minimal reproducer. */
public final class EmptyServer {

    static void main(String[] args) {
        DemoServer.create()
                .feature(new LobbyFeature())
                .start();
    }
}
