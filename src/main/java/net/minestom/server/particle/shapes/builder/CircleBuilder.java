package net.minestom.server.particle.shapes.builder;

import net.minestom.server.particle.shapes.ParticleCircle;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

public class CircleBuilder {
    private Position position = new Position();
    private double radius = 1;
    private ParticleCircle.Facing facing = ParticleCircle.Facing.Y;

    public @NotNull CircleBuilder position(@NotNull Position position) {
        this.position = position;
        return this;
    }

    public @NotNull CircleBuilder radius(double radius) {
        this.radius = radius;
        return this;
    }

    public @NotNull CircleBuilder facing(@NotNull ParticleCircle.Facing facing) {
        this.facing = facing;
        return this;
    }

    public @NotNull ParticleCircle build() {
        return new ParticleCircle(position.getX(), position.getY(), position.getZ(), radius, facing);
    }
}
