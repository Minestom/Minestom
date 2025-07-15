package net.minestom.server.instance.generator;


import java.util.Collection;

@FunctionalInterface
public interface Generator {
    /**
     * This method is called when this generator is requesting this unit to be filled with blocks or biomes.
     *
     * @param unit the unit to fill
     */
    void generate(GenerationUnit unit);

    /**
     * Runs {@link #generate(GenerationUnit)} on each unit in the collection.
     *
     * @param units the list of units to fill
     */
    default void generateAll(Collection<GenerationUnit> units) {
        units.forEach(this::generate);
    }
}
