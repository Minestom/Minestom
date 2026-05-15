package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.feature.blocks.BlocksFeature;

/**
 * Block-system showcase: placement rules, handlers, bed/door/waterlog
 * mechanics, sign editing. Use this to reproduce block-specific issues
 * without dragging entity/recipe/chat surface area into the repro.
 */
public final class BlocksServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new BlocksFeature()
        ).start();
    }
}
