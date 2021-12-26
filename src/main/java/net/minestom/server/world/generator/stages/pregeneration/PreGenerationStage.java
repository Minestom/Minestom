package net.minestom.server.world.generator.stages.pregeneration;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.world.generator.GenerationContext;
import net.minestom.server.world.generator.stages.StageDataDependent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Pre-generation stages are responsible for building a context around the requested section, this allows
 * the generation of large structures or just simple trees that span multiple sections.
 *
 * @param <T> the type of the data this stage provides
 */
public interface PreGenerationStage<T extends StageData> extends StageDataDependent {
    int INTERNAL_STAGE_ID_OFFSET = 1000;
    /**
     * Called for sections in the {@link #getRange() look around} range around the generated sectionData<br>
     * NOTE: This will be called for each sectionData in a cubical radius instead of a spherical one!
     * @param context the generator, used to get results of previous stages and save results of this stage
     * @param sectionX sectionData's X coordinate
     * @param sectionY sectionData's Y coordinate
     * @param sectionZ sectionData's Z coordinate
     */
    void process(GenerationContext context, int sectionX, int sectionY, int sectionZ);

    /**
     * The range in what the generator should look around, e.g. for generating structures
     * @return range > 0
     */
    int getRange();

    /**
     * This id is used to save/retrieve generation data<br>
     * <b>WARNING: id range 1000-1500 is reserved for Minestom defined stages</b>
     * @return generator level unique id
     */
    int getUniqueId();

    /**
     * Used to handle old generation data, should be bumped if the data type changes
     * @return version number of the stage
     */
    int getVersion();

    /**
     * Reader used to load back saved generation data
     * @return the reader or {@code null} if this stage doesn't support saving data
     */
    @Nullable Function<BinaryReader, T> getDataReader();

    /**
     * This is used by {@link #getType()} to define the scope of this stage.
     * @return the data class this stage provides
     */
    @NotNull Class<T> getDataClass();

    /**
     * Used to find the scope of the stage
     * @return the {@link Type} of the stage
     */
    default Type getType() {
        if (StageData.Instance.class.isAssignableFrom(getDataClass())) {
            return Type.INSTANCE;
        } else if (StageData.Chunk.class.isAssignableFrom(getDataClass())) {
            return Type.CHUNK;
        } else if (StageData.Section.class.isAssignableFrom(getDataClass())) {
            return Type.SECTION;
        }
        throw new IllegalStateException("How did we get here?");
    }

    enum Type {
        INSTANCE, CHUNK, SECTION
    }
}
