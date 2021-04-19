package net.minestom.server.particle.shapes;

import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PolygonBuilder {
    private List<Position> points = new ArrayList<>();

    public @NotNull PolygonBuilder points(@NotNull List<Position> points) {
        this.points = points;
        return this;
    }

    public @NotNull PolygonBuilder addPoint(@NotNull Position point) {
        this.points.add(point);
        return this;
    }

    public @NotNull ParticlePolygon build() {
        return new ParticlePolygon(points.toArray(Position[]::new));
    }
}
