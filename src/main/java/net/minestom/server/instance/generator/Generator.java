package net.minestom.server.instance.generator;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface Generator {
    /**
     * This method is called when this generator is requesting this unit to be filled with blocks or biomes.
     *
     * @param unit the unit to fill
     */
    void generate(@NotNull GenerationUnit unit);

    /**
     * Runs {@link #generate(GenerationUnit)} on each unit in the collection.
     *
     * @param units the list of units to fill
     */
    default void generateAll(@NotNull Collection<@NotNull GenerationUnit> units) {
        units.forEach(this::generate);
    }
}
