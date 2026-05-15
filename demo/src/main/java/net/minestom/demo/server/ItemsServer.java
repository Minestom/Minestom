package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.feature.items.ItemsFeature;
import net.minestom.demo.feature.projectile.ProjectileFeature;
import net.minestom.demo.feature.recipe.RecipeFeature;

/**
 * Inventory / item-stack showcase: bundles, consumables, can-place-on /
 * can-break predicates, drop physics, creative apple swap, crossbow
 * charging, and the demo shapeless recipe.
 */
public final class ItemsServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new ItemsFeature(),
                new ProjectileFeature(),
                new RecipeFeature()
        ).start();
    }
}
