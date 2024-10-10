package net.minestom.server.instance.batch;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.location.LocationUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * an interface used in {@link TransformedBlockBatch} to apply transformations.
 */
public interface BlockTransformer {
    BlockTransformer NIL = new BlockTransformer() {};

    default @NotNull BlockVec transformPosition(int x, int y, int z) {
        return new BlockVec(x, y, z);
    }

    /**
     * Transforms the position index of some block.
     * {@link TransformedBlockBatch} will usually call into this, so it can be overridden to avoid allocation.
     *
     * @param index The original global block index to transform
     * @return The transformed block index.
     *
     * @see LocationUtils#getGlobalBlockIndex(int, int, int)
     */
    @ApiStatus.Internal
    default long transformPositionIndex(long index) {
        final BlockVec position = transformPosition(
                LocationUtils.globalBlockIndexToPositionX(index),
                LocationUtils.globalBlockIndexToPositionY(index),
                LocationUtils.globalBlockIndexToPositionZ(index));
        return LocationUtils.getGlobalBlockIndex(position);
    }

    default @NotNull Block transformBlock(@NotNull Block block) {
        return block;
    }

    record Pipeline(List<BlockTransformer> transformers) implements BlockTransformer {
        @Override
        public @NotNull BlockVec transformPosition(int x, int y, int z) {
            return LocationUtils.getGlobalBlockPosition(
                    transformPositionIndex(LocationUtils.getGlobalBlockIndex(x, y, z)));
        }

        @Override
        public long transformPositionIndex(long index) {
            for (BlockTransformer transformer : transformers) {
                index = transformer.transformPositionIndex(index);
            }
            return index;
        }

        @Override
        public @NotNull Block transformBlock(@NotNull Block block) {
            for (BlockTransformer transformer : transformers) {
                block = transformer.transformBlock(block);
            }
            return block;
        }
    }
}
