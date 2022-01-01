package net.minestom.server.instance.generator;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface GenerationUnit {
    @NotNull UnitModifier modifier();

    @NotNull Point size();

    @NotNull Point absoluteStart();

    @NotNull Point absoluteEnd();

    /**
     * Divides this unit into the smallest independent units.
     *
     * @return an immutable list of independent units
     */
    default @NotNull List<GenerationUnit> subdivide() {
        return List.of(this);
    }
}
