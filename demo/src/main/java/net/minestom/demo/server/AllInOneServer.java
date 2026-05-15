package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.core.ServerListPingFeature;
import net.minestom.demo.feature.benchmark.BenchmarkFeature;
import net.minestom.demo.feature.blocks.BlocksFeature;
import net.minestom.demo.feature.chat.ChatFeature;
import net.minestom.demo.feature.combat.CombatFeature;
import net.minestom.demo.feature.debug.DebugFeature;
import net.minestom.demo.feature.dialog.DialogFeature;
import net.minestom.demo.feature.display.DisplayFeature;
import net.minestom.demo.feature.entities.EntitiesFeature;
import net.minestom.demo.feature.input.InputFeature;
import net.minestom.demo.feature.items.ItemsFeature;
import net.minestom.demo.feature.networking.NetworkingFeature;
import net.minestom.demo.feature.projectile.ProjectileFeature;
import net.minestom.demo.feature.recipe.RecipeFeature;
import net.minestom.demo.feature.transfer.TransferFeature;
import net.minestom.demo.feature.world.WorldFeature;

/**
 * Parity launcher: same surface area as the old monolithic
 * {@code Main} — every feature on, one port, offline auth, LAN broadcast.
 * <p>
 * Bound to {@code 0.0.0.0:25565}. Use the per-feature launchers in this
 * package for smaller, focused reproducers.
 */
public final class AllInOneServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new ServerListPingFeature(),
                new ChatFeature(),
                new DialogFeature(),
                new InputFeature(),
                new NetworkingFeature(),
                new ItemsFeature(),
                new ProjectileFeature(),
                new RecipeFeature(),
                new CombatFeature(),
                new BlocksFeature(),
                new EntitiesFeature(),
                new WorldFeature(),
                new DisplayFeature(),
                new TransferFeature(),
                new BenchmarkFeature(),
                new DebugFeature()
        ).start();
    }
}
