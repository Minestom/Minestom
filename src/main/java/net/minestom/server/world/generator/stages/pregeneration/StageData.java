package net.minestom.server.world.generator.stages.pregeneration;

import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

public interface StageData extends Writeable {
    //TODO Default implementation returning false?
    boolean supportsSaving();

    @Override
    default void write(@NotNull BinaryWriter writer) {
        if (supportsSaving()) {
            throw new IllegalStateException("Savable StageData doesn't implement the writer method!");
        } else {
            throw new IllegalStateException("Saving isn't supported for this data!");
        }
    }

    abstract class Instance implements StageData {}
    abstract class Chunk implements StageData {}
    abstract class Section implements StageData {}
}
