package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.feature.combat.CombatFeature;
import net.minestom.demo.feature.projectile.ProjectileFeature;

/** Combat + projectile. */
public final class CombatServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new CombatFeature(),
                new ProjectileFeature()
        ).start();
    }
}
