package net.minestom.server.instance.block;

import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static net.minestom.server.instance.block.BlockBatchImpl.BuilderImpl;
import static net.minestom.server.instance.block.BlockBatchImpl.OptionImpl;

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
    static @NotNull BlockBatch explicit(@NotNull Consumer<@NotNull Builder> consumer) {
        final Option option = new OptionImpl(false, false);
        return batch(option, consumer);
    }

    /**
     * Block batch that only place explicitly set blocks, ignoring nbt and handlers.
     */
    static @NotNull BlockBatch explicitStates(@NotNull Consumer<@NotNull Builder> consumer) {
        final Option option = new OptionImpl(true, false);
        return batch(option, consumer);
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
         * Whether to consider unset blocks at section boundaries as air implicitly, improving performance.
         * <p>
         * Has the potential to increase performance by allowing direct palette copies.
         *
         * @return true if unset blocks are considered air implicitly
         */
        boolean implicitAirBoundaries();
    }

    sealed interface Builder extends Block.Setter permits BuilderImpl {
        void copyPalette(int sectionX, int sectionY, int sectionZ, @NotNull Palette palette);
    }
}
