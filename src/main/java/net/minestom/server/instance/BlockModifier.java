package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.instance.batch.BlockBatch;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.validate.Check;

/**
 * Represents an element which can place blocks at position.
 * <p>
 * Notably used by {@link Instance}, {@link BlockBatch} and {@link ChunkBatch}.
 */
public interface BlockModifier {

    BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    /**
     * Sets a block at a position.
     * <p>
     * You can use {@link #setBlock(int, int, int, Block)} if you want it to be more explicit.
     *
     * @param x            the block X
     * @param y            the block Y
     * @param z            the block Z
     * @param blockStateId the block state id
     * @param data         the block {@link Data}, can be null
     */
    void setBlockStateId(int x, int y, int z, short blockStateId, Data data);

    /**
     * Sets a {@link CustomBlock} at a position.
     * <p>
     * The custom block id should be the one returned by {@link CustomBlock#getCustomBlockId()}.
     *
     * @param x             the block X
     * @param y             the block Y
     * @param z             the block Z
     * @param customBlockId the custom block id
     * @param data          the block {@link Data}, can be null
     */
    void setCustomBlock(int x, int y, int z, short customBlockId, Data data);

    /**
     * Sets a {@link CustomBlock} at a position with a custom state id.
     * <p>
     * The custom block id should be the one returned by {@link CustomBlock#getCustomBlockId()},
     * and the block state id can be anything you want, state id can be retrieved using {@link Block#getBlockId()}.
     *
     * @param x             the block X
     * @param y             the block Y
     * @param z             the block Z
     * @param blockStateId  the block state id
     * @param customBlockId the custom block id
     * @param data          the block {@link Data}, can be null
     */
    void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId, Data data);

    default void setBlockStateId(int x, int y, int z, short blockStateId) {
        setBlockStateId(x, y, z, blockStateId, null);
    }

    default void setBlock(int x, int y, int z, Block block) {
        setBlockStateId(x, y, z, block.getBlockId(), null);
    }

    default void setBlock(BlockPosition blockPosition, Block block) {
        Check.notNull(blockPosition, "The block position cannot be null");
        Check.notNull(block, "The block cannot be null");
        setBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), block);
    }

    default void setBlockStateId(BlockPosition blockPosition, short blockStateId) {
        Check.notNull(blockPosition, "The block position cannot be null");
        setBlockStateId(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), blockStateId);
    }

    default void setCustomBlock(int x, int y, int z, short customBlockId) {
        setCustomBlock(x, y, z, customBlockId, null);
    }

    default void setCustomBlock(int x, int y, int z, String customBlockId, Data data) {
        CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        Check.notNull(customBlock, "The CustomBlock " + customBlockId + " is not registered");

        setCustomBlock(x, y, z, customBlock.getCustomBlockId(), data);
    }

    default void setCustomBlock(int x, int y, int z, String customBlockId) {
        setCustomBlock(x, y, z, customBlockId, null);
    }

    default void setCustomBlock(BlockPosition blockPosition, String customBlockId) {
        Check.notNull(blockPosition, "The block position cannot be null");
        setCustomBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), customBlockId);
    }

    default void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId) {
        setSeparateBlocks(x, y, z, blockStateId, customBlockId, null);
    }

    default void setSeparateBlocks(BlockPosition blockPosition, short blockStateId, short customBlockId) {
        Check.notNull(blockPosition, "The block position cannot be null");
        setSeparateBlocks(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), blockStateId, customBlockId, null);
    }

}
