package net.minestom.server.instance;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static net.minestom.server.instance.BlockBatchImpl.BuilderImpl;

@ApiStatus.Experimental
public sealed interface BlockBatch extends Block.Getter permits BlockBatchImpl {
    long NO_FLAGS = 0L;               // No flags set
    long IGNORE_DATA_FLAG = 1L;       // Ignore NBT and handlers
    long ALIGNED_FLAG = 1L << 1;      // Section-aligned optimization
    long GENERATE_FLAG = 1L << 2;     // Generate world if unloaded

    static @NotNull BlockBatch batch(@MagicConstant(flagsFromClass = BlockBatch.class) long flags,
                                     @NotNull Consumer<@NotNull Builder> consumer) {
        BuilderImpl builder = new BuilderImpl(flags);
        consumer.accept(builder);
        return builder.build();
    }

    /**
     * Block batch that only place explicitly set blocks.
     * <p>
     * Most flexible option, but also the least performant.
     */
    static @NotNull BlockBatch unaligned(@NotNull Consumer<@NotNull Builder> consumer) {
        return batch(GENERATE_FLAG, consumer);
    }

    /**
     * Block batch that only place explicitly set blocks, ignoring nbt and handlers.
     */
    static @NotNull BlockBatch unalignedStates(@NotNull Consumer<@NotNull Builder> consumer) {
        return batch(IGNORE_DATA_FLAG | GENERATE_FLAG, consumer);
    }

    static @NotNull BlockBatch aligned(@NotNull Consumer<@NotNull Builder> consumer) {
        return batch(ALIGNED_FLAG | GENERATE_FLAG, consumer);
    }

    static @NotNull BlockBatch alignedStates(@NotNull Consumer<@NotNull Builder> consumer) {
        return batch(IGNORE_DATA_FLAG | ALIGNED_FLAG | GENERATE_FLAG, consumer);
    }

    static @NotNull BlockBatch empty() {
        return batch(0, builder -> {
        });
    }

    void getAll(@NotNull EntryConsumer consumer);

    /**
     * Counts the number of blocks in this batch.
     * <p>
     * May not be representative of its performance, as it does not account for
     * the number of sections or palette entries.
     */
    int count();

    long flags();

    /**
     * Whether the batch only contains state data, not block instances.
     * <p>
     * This can be used to optimize certain operations that do not require full block instances.
     *
     * @return true if the batch only contains state data
     */
    default boolean ignoreData() {
        return (flags() & IGNORE_DATA_FLAG) != 0;
    }

    /**
     * Whether the batch is section-aligned, meaning that it implicitly considers unset blocks as air.
     * <p>
     * This is useful for performance optimizations, as entire sections can be copied
     * without checking each block individually.
     *
     * @return true if section aligned
     */
    default boolean aligned() {
        return (flags() & ALIGNED_FLAG) != 0;
    }

    /**
     * Whether the batch should do world generation if unloaded.
     */
    default boolean generate() {
        return (flags() & GENERATE_FLAG) != 0;
    }

    @FunctionalInterface
    interface EntryConsumer {
        void accept(int x, int y, int z, @NotNull Block block);
    }

    sealed interface Builder extends Block.Setter permits BuilderImpl {
        void copyPalette(int sectionX, int sectionY, int sectionZ, @NotNull Palette palette);
    }
}
