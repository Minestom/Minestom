package net.minestom.server.instance;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static net.minestom.server.instance.BlockBatchImpl.BuilderImpl;
import static net.minestom.server.instance.BlockBatchImpl.OptionImpl;

@ApiStatus.Experimental
public sealed interface BlockBatch extends Block.Getter permits BlockBatchImpl {
    static @NotNull BlockBatch batch(@NotNull Option option, @NotNull Consumer<@NotNull Builder> consumer) {
        BuilderImpl builder = new BuilderImpl(option);
        consumer.accept(builder);
        return builder.build();
    }

    /**
     * Block batch that only place explicitly set blocks.
     * <p>
     * Most flexible option, but also the least performant.
     */
    static @NotNull BlockBatch unaligned(@NotNull Consumer<@NotNull Builder> consumer) {
        final Option option = new OptionImpl(false, false, true);
        return batch(option, consumer);
    }

    /**
     * Block batch that only place explicitly set blocks, ignoring nbt and handlers.
     */
    static @NotNull BlockBatch unalignedStates(@NotNull Consumer<@NotNull Builder> consumer) {
        final Option option = new OptionImpl(true, false, true);
        return batch(option, consumer);
    }

    static @NotNull BlockBatch aligned(@NotNull Consumer<@NotNull Builder> consumer) {
        final Option option = new OptionImpl(false, true, true);
        return batch(option, consumer);
    }

    static @NotNull BlockBatch alignedStates(@NotNull Consumer<@NotNull Builder> consumer) {
        final Option option = new OptionImpl(true, true, true);
        return batch(option, consumer);
    }

    static @NotNull BlockBatch empty() {
        final Option option = new OptionImpl(true, true, false);
        return batch(option, builder -> {
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

    @NotNull Option option();

    @FunctionalInterface
    interface EntryConsumer {
        void accept(int x, int y, int z, @NotNull Block block);
    }

    sealed interface Option permits OptionImpl {
        /**
         * Whether the batch only contains state data, not block instances.
         * <p>
         * This can be used to optimize certain operations that do not require full block instances.
         *
         * @return true if the batch only contains state data
         */
        boolean onlyState();

        /**
         * Whether the batch is section-aligned, meaning that it implicitly considers unset blocks as air.
         * <p>
         * This is useful for performance optimizations, as entire sections can be copied
         * without checking each block individually.
         *
         * @return true if section aligned
         */
        boolean aligned();

        /**
         * Whether the batch should do world generation if unloaded.
         */
        boolean generate();
    }

    sealed interface Builder extends Block.Setter permits BuilderImpl {
        void copyPalette(int sectionX, int sectionY, int sectionZ, @NotNull Palette palette);
    }
}
