package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.feature.blocks.BlocksFeature;

/** Block placement rules, handlers, and interaction mechanics. */
public final class BlocksServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new BlocksFeature()
        ).start();
    }
}
