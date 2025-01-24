package net.minestom.server.particle;

import net.minestom.server.color.AlphaColor;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class ParticleImpl {
    private static final Registry.Container<Particle> CONTAINER = Registry.createStaticContainer(Registry.Resource.PARTICLES,
            (namespace, properties) -> defaultParticle(NamespaceID.from(namespace), properties.getInt("id")));

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

    private static Particle defaultParticle(@NotNull NamespaceID namespace, int id) {
        return switch (namespace.asString()) {
            case "minecraft:block" -> new Particle.Block(namespace, id, Block.STONE);
            case "minecraft:block_marker" -> new Particle.BlockMarker(namespace, id, Block.STONE);
            case "minecraft:falling_dust" -> new Particle.FallingDust(namespace, id, Block.STONE);
            case "minecraft:dust_pillar" -> new Particle.DustPillar(namespace, id, Block.STONE);
            case "minecraft:dust" -> new Particle.Dust(namespace, id, Color.WHITE, 1);
            case "minecraft:dust_color_transition" -> new Particle.DustColorTransition(namespace, id, Color.WHITE, Color.WHITE, 1);
            case "minecraft:sculk_charge" -> new Particle.SculkCharge(namespace, id, 0);
            case "minecraft:item" -> new Particle.Item(namespace, id, ItemStack.AIR);
            case "minecraft:vibration" -> new Particle.Vibration(namespace, id, Particle.Vibration.SourceType.BLOCK, Vec.ZERO, 0, 0, 0);
            case "minecraft:shriek" -> new Particle.Shriek(namespace, id, 0);
            case "minecraft:entity_effect" -> new Particle.EntityEffect(namespace, id, AlphaColor.WHITE);
            case "minecraft:trail" -> new Particle.Trail(namespace, id, Vec.ZERO, Color.WHITE);
            case "minecraft:block_crumble" -> new Particle.BlockCrumble(namespace, id, Block.STONE);
            default -> new Particle.Simple(namespace, id);
        };
    }

    private ParticleImpl() {
    }
}
