package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.feature.benchmark.BenchmarkFeature;
import net.minestom.demo.feature.world.WorldFeature;

/**
 * World-state showcase: weather, world border, dimension switching,
 * sleep, save, plus a live tick/RAM HUD via {@link BenchmarkFeature}.
 */
public final class WorldServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new WorldFeature(),
                new BenchmarkFeature()
        ).start();
    }
}
