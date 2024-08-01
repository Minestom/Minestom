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
            (key, properties) -> defaultParticle(Key.key(key), properties.getInt("id")));

    static Particle get(@NotNull String key) {
        return CONTAINER.get(key);
    }

    static Particle getSafe(@NotNull String key) {
        return CONTAINER.getSafe(key);
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
            case "minecraft:dust" -> new Particle.Dust(key, id, new Color(255, 255, 255), 1);
            case "minecraft:dust_color_transition" -> new Particle.DustColorTransition(key, id, new Color(255, 255, 255),
                    new Color(255, 255, 255), 1);
            case "minecraft:sculk_charge" -> new Particle.SculkCharge(key, id, 0);
            case "minecraft:item" -> new Particle.Item(key, id, ItemStack.AIR);
            case "minecraft:vibration" -> new Particle.Vibration(key, id, Particle.Vibration.SourceType.BLOCK, Vec.ZERO, 0, 0, 0);
            case "minecraft:shriek" -> new Particle.Shriek(key, id, 0);
            case "minecraft:entity_effect" -> new Particle.EntityEffect(key, id, new AlphaColor(255, 0, 0, 0));
            default -> new Particle.Simple(key, id);
        };
    }

    private ParticleImpl() {
    }
}
