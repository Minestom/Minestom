package net.minestom.server.raw_data;

import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class RawBlockData {
    private final double explosionResistance;
    private final @NotNull Supplier<@NotNull Material> item;
    private final double friction;
    private final double speedFactor;
    private final double jumpFactor;

    public RawBlockData(
            double explosionResistance,
            @NotNull Supplier<@NotNull Material> item,
            double friction,
            double speedFactor,
            double jumpFactor
    ) {
        this.explosionResistance = explosionResistance;
        this.item = item;
        this.friction = friction;
        this.speedFactor = speedFactor;
        this.jumpFactor = jumpFactor;
    }

    public double getExplosionResistance() {
        return explosionResistance;
    }

    @NotNull
    public Material getItem() {
        return item.get();
    }

    public double getFriction() {
        return friction;
    }

    public double getSpeedFactor() {
        return speedFactor;
    }

    public double getJumpFactor() {
        return jumpFactor;
    }
}