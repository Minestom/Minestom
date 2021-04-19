package net.minestom.server.particle.shapes;

import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

public abstract class ParticleShape {
    public abstract ParticleIterator<?> iterator(int particleCount);

    public static @NotNull PolygonBuilder polygon() {
        return new PolygonBuilder();
    }

    public static @NotNull MultiPolygonBuilder multiPolygon() {
        return new MultiPolygonBuilder();
    }

    public static @NotNull MultiPolygon cube(Position position, double width, double height, double depth) {
        return multiPolygon()
                .lineStart(position)
                .lineTo(position.add(width, 0, 0))
                .lineTo(position.add(width, 0, depth))
                .lineTo(position.add(0, 0, depth))

                .jumpTo(position.add(0, height, 0))
                .lineTo(position.add(width, height, 0))
                .lineTo(position.add(width, height, depth))
                .lineTo(position.add(0, height, depth))

                .jumpTo(position)
                .lineTo(position.add(0, height, 0))
                .jumpTo(position.add(width, 0, 0))
                .lineTo(position.add(width, height, 0))
                .jumpTo(position.add(width, 0, depth))
                .lineTo(position.add(width, height, depth))
                .jumpTo(position.add(0, 0, depth))
                .lineTo(position.add(0, height, depth))

                .build();
    }
}
