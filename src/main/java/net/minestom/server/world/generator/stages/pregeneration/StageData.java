package net.minestom.server.world.generator.stages.pregeneration;

import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

public sealed interface StageData extends Writeable permits StageData.Chunk, StageData.Instance, StageData.Section {
    //TODO Default implementation returning false?
    default boolean supportsSaving() {
        return false;
    }

    /**
     * Used to check if the data is present because it's overflown or because it is generated for that specific location
     * @return {@code true} if it is generated
     */
    default boolean generated() {
        return true;
    }

    @Override
    default void write(@NotNull BinaryWriter writer) {
        if (supportsSaving()) {
            throw new IllegalStateException("Savable StageData doesn't implement the writer method!");
        } else {
            throw new IllegalStateException("Saving isn't supported for this data!");
        }
    }

    non-sealed interface Instance extends StageData {}
    non-sealed interface Chunk extends StageData {}
    non-sealed interface Section extends StageData {}
}
