package net.minestom.server.particle.shapes.builder;

import net.minestom.server.particle.shapes.MultiPolygon;
import net.minestom.server.particle.shapes.ParticleShape;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MultiPolygonBuilder {
    private final List<ParticleShape> completedShapes = new ArrayList<>();
    private PolygonBuilder lastPolygon;

    public @NotNull MultiPolygonBuilder lineStart(@NotNull Position position) {
        lastPolygon = new PolygonBuilder().addPoint(position);
        return this;
    }

    public @NotNull MultiPolygonBuilder lineToLastStart() {
        Check.stateCondition(lastPolygon == null, "Cannot use lineToLastStart when no starting point is specified");
        completedShapes.add(lastPolygon.close(true).build());
        lastPolygon = null;
        return this;
    }

    public @NotNull MultiPolygonBuilder lineTo(@NotNull Position position) {
        Check.stateCondition(lastPolygon == null, "Cannot use lineTo when no starting point is specified");
        lastPolygon.addPoint(position);
        return this;
    }

    public @NotNull MultiPolygonBuilder jumpTo(@NotNull Position position) {
        return endLine().lineStart(position);
    }

    public @NotNull MultiPolygonBuilder addShape(@NotNull ParticleShape shape) {
        endLine();
        completedShapes.add(shape);
        return this;
    }

    public @NotNull MultiPolygon build() {
        endLine();
        return new MultiPolygon(completedShapes.toArray(ParticleShape[]::new));
    }

    private @NotNull MultiPolygonBuilder endLine() {
        if (lastPolygon != null) {
            completedShapes.add(lastPolygon.close(false).build());
            lastPolygon = null;
        }
        return this;
    }
}
