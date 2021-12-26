package net.minestom.server.world.generator.stages.pregeneration;

public sealed interface StageData permits StageData.Chunk, StageData.Instance, StageData.Section {
    /**
     * Used to check if the data is present because it's overflown or because it is generated for that specific location
     * @return {@code true} if it is generated
     */
    default boolean generated() {
        return true;
    }

    non-sealed interface Instance extends StageData {}
    non-sealed interface Chunk extends StageData {}
    non-sealed interface Section extends StageData {}
}
