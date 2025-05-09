package net.minestom.server.particle;

import net.kyori.adventure.key.Key;
import net.minestom.server.color.AlphaColor;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class ParticleImpl {
    private static final Registry.Container<Particle> CONTAINER = Registry.createStaticContainer(Registry.Resource.PARTICLES,
            (namespace, properties) -> defaultParticle(Key.key(namespace), properties.getInt("id")));

    static Particle get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static Particle getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static Particle getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<Particle> values() {
        return CONTAINER.values();
    }

    private static Particle defaultParticle(@NotNull Key key, int id) {
        return switch (key.asString()) {
            case "minecraft:block" -> new Particle.Block(key, id, Block.STONE);
            case "minecraft:block_marker" -> new Particle.BlockMarker(key, id, Block.STONE);
            case "minecraft:falling_dust" -> new Particle.FallingDust(key, id, Block.STONE);
            case "minecraft:dust_pillar" -> new Particle.DustPillar(key, id, Block.STONE);
            case "minecraft:dust" -> new Particle.Dust(key, id, Color.WHITE, 1);
            case "minecraft:dust_color_transition" -> new Particle.DustColorTransition(key, id, Color.WHITE, Color.WHITE, 1);
            case "minecraft:sculk_charge" -> new Particle.SculkCharge(key, id, 0);
            case "minecraft:item" -> new Particle.Item(key, id, ItemStack.AIR);
            case "minecraft:vibration" -> new Particle.Vibration(key, id, Particle.Vibration.SourceType.BLOCK, Vec.ZERO, 0, 0, 0);
            case "minecraft:shriek" -> new Particle.Shriek(key, id, 0);
            case "minecraft:entity_effect" -> new Particle.EntityEffect(key, id, AlphaColor.WHITE);
            case "minecraft:trail" -> new Particle.Trail(key, id, Vec.ZERO, Color.WHITE, 0);
            case "minecraft:block_crumble" -> new Particle.BlockCrumble(key, id, Block.STONE);
            case "minecraft:tinted_leaves" -> new Particle.TintedLeaves(namespace, id, AlphaColor.WHITE);
            default -> new Particle.Simple(key, id);
        };
    }

    private ParticleImpl() {
    }
}
