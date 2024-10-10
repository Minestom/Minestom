package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.location.LocationUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * A {@link Batch} which can be when changes are required across chunk borders,
 * and are going to be transformed by some kind of function.
 * Different transformation functions can be applied at insertion into the batch, and on application of the batch.
 * <p>
 * If transformation is not required, {@link AbsoluteBlockBatch} should be used for efficiency.
 * If only the entire batch needs to be moved, use {@link RelativeBlockBatch}.
 * <p>
 * All inverses are {@link AbsoluteBlockBatch}s and represent the inverse of the application
 * with all transformations applied.
 * <p>
 * If the batch will be used multiple times with the same end transformer, it is suggested
 * to convert it to an {@link AbsoluteBlockBatch} and cache the result. Application
 * of absolute batches (currently) is significantly faster than their transformed counterpart.
 *
 * @see Batch
 * @see AbsoluteBlockBatch
 * @see RelativeBlockBatch
 */
@ApiStatus.Experimental
public class TransformedBlockBatch implements Batch {
    // Need to be synchronized manually (with 'this' because transformation)
    // Format: global position index - block
    private Long2ObjectMap<Block> blockMap = new Long2ObjectOpenHashMap<>();

    private final BatchOption options;
    private final BatchOption inverseOption;
    private final BlockTransformer insertTransformer;

    public TransformedBlockBatch(@NotNull BlockTransformer insertTransformer) {
        this(insertTransformer, new BatchOption());
    }

    public TransformedBlockBatch(@NotNull BlockTransformer insertTransformer, @NotNull BatchOption options) {
        this(insertTransformer, options, new BatchOption());
    }

    public TransformedBlockBatch(
            @NotNull BlockTransformer insertTransformer,
            @NotNull BatchOption options,
            @NotNull BatchOption inverseOption
    ) {
        this.options = options;
        this.inverseOption = inverseOption;
        this.insertTransformer = insertTransformer;
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        block = insertTransformer.transformBlock(block);

        final long index = insertTransformer.transformPositionIndex(LocationUtils.getGlobalBlockIndex(x, y, z));

        synchronized (this) {
            this.blockMap.put(index, block);
        }
    }

    /**
     * Transforms all blocks in this batch with the given transformer.
     *
     * @param transformer The transformer applied to all blocks
     */
    public void transform(@NotNull BlockTransformer transformer) {
        final Long2ObjectMap<Block> blockMap = new Long2ObjectOpenHashMap<>();
        synchronized (this) {
            transformIntoMap(transformer, blockMap);
            this.blockMap = blockMap;
        }
    }

    /**
     * Creates a new batch and transforms all blocks in this batch into it.
     *
     * @param transformer The transformer applied to all blocks
     * @return A new batch filled with the transformed blocks
     */
    public @NotNull TransformedBlockBatch transformTo(@NotNull BlockTransformer transformer) {
        final var result = new TransformedBlockBatch(insertTransformer, options, inverseOption);
        synchronized (this) {
            transformIntoMap(transformer, result.blockMap);
        }
        return result;
    }

    /**
     * Looks at each block in the batch, transforms it, and puts it in `blockMap`.
     * @param transformer The transformer to apply.
     * @param blockMap The blockMap to insert the transformed blocks into.
     */
    protected void transformIntoMap(@NotNull BlockTransformer transformer, @NotNull Long2ObjectMap<Block> blockMap) {
        for (var entry : Long2ObjectMaps.fastIterable(this.blockMap)) {
            final long index = entry.getLongKey();
            final Block block = transformer.transformBlock(entry.getValue());
            blockMap.put(transformer.transformPositionIndex(index), block);
        }
    }

    @Override
    public @NotNull CompletableFuture<@Nullable AbsoluteBlockBatch> apply(@NotNull Instance instance) {
        return apply(instance, BlockTransformer.NIL);
    }

    /**
     * Transforms this batch with the given transformer and applies it to the given instance.
     *
     * @param instance The instance to apply this batch to
     * @param transformer The transformer to transform this batch.
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public @NotNull CompletableFuture<@Nullable AbsoluteBlockBatch> apply(
            @NotNull Instance instance,
            @NotNull BlockTransformer transformer
    ) {
        return CompletableFuture.supplyAsync(() -> this.toAbsoluteBatch(transformer).apply(instance).join());
    }

    /**
     * Converts this batch into an {@link AbsoluteBlockBatch} as is.
     *
     * @return An absolute batch from this batch as is.
     */
    @NotNull
    public AbsoluteBlockBatch toAbsoluteBatch() {
        return toAbsoluteBatch(BlockTransformer.NIL);
    }

    /**
     * Converts this batch into an {@link AbsoluteBlockBatch} with the given transformer.
     *
     * @param transformer The transformer to apply
     * @return An absolute batch with the transformer applied.
     */
    @NotNull
    public AbsoluteBlockBatch toAbsoluteBatch(@NotNull BlockTransformer transformer) {
        final AbsoluteBlockBatch batch = new AbsoluteBlockBatch(this.options, this.inverseOption);

        synchronized (this) {
            for (var entry : Long2ObjectMaps.fastIterable(blockMap)) {
                final long index = entry.getLongKey();

                final Block block = transformer.transformBlock(entry.getValue());

                final BlockVec position = insertTransformer.transformPosition(
                        LocationUtils.globalBlockIndexToPositionX(index),
                        LocationUtils.globalBlockIndexToPositionY(index),
                        LocationUtils.globalBlockIndexToPositionZ(index));

                // this batch was made in the function, no one else has it
                batch.UNSAFE_setBlock(position.blockX(), position.blockY(), position.blockZ(), block);
            }
        }
        return batch;
    }
}
