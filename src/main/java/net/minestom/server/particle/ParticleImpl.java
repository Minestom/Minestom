package net.minestom.server.particle;

import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collection;

sealed class ParticleImpl implements Particle permits BlockParticle, DustParticle, DustColorTransitionParticle,
        SculkChargeParticle, ItemParticle, VibrationParticle, ShriekParticle {
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

    private final NamespaceID namespace;
    private final int id;

    ParticleImpl(@NotNull NamespaceID namespace, int id) {
        this.namespace = namespace;
        this.id = id;
    }

    @Override
    public @NotNull NamespaceID namespace() {
        return namespace;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public @NotNull ParticleImpl readData(@NotNull NetworkBuffer reader) {
        return this;
    }

    @Override
    public void writeData(@NotNull NetworkBuffer writer) {
    }

    @Override
    public @NotNull String toString() {
        return name();
    }

    private static Particle defaultParticle(@NotNull NamespaceID namespace, int id) {
        return switch (namespace.asString()) {
            case "minecraft:block", "minecraft:block_marker", "minecraft:falling_dust" -> new BlockParticle(namespace, id, Block.STONE);
            case "minecraft:dust" -> new DustParticle(namespace, id, new Color(255, 255, 255), 1);
            case "minecraft:dust_color_transition" -> new DustColorTransitionParticle(namespace, id, new Color(255, 255, 255),
                    1, new Color(255, 255, 255));
            case "minecraft:sculk_charge" -> new SculkChargeParticle(namespace, id, 0);
            case "minecraft:item" -> new ItemParticle(namespace, id, ItemStack.AIR);
            case "minecraft:vibration" -> new VibrationParticle(namespace, id, VibrationParticle.SourceType.BLOCK, Vec.ZERO, 0, 0, 0);
            case "minecraft:shriek" -> new ShriekParticle(namespace, id, 0);
            default -> new ParticleImpl(namespace, id);
        };
    }
}
