package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;

public interface BlockModifier {

    BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    void setBlock(int x, int y, int z, short blockId, Data data);

    void setCustomBlock(int x, int y, int z, short customBlockId, Data data);

    default void setBlock(int x, int y, int z, short blockId) {
        setBlock(x, y, z, blockId, null);
    }

    default void setBlock(int x, int y, int z, Block block) {
        setBlock(x, y, z, block.getBlockId(), null);
    }

    default void setCustomBlock(int x, int y, int z, short customBlockId) {
        setCustomBlock(x, y, z, customBlockId, null);
    }

    default void setBlock(BlockPosition blockPosition, Block block) {
        setBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), block.getBlockId());
    }

    default void setBlock(BlockPosition blockPosition, short blockId) {
        setBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), blockId);
    }

    default void setBlock(Position position, Block block) {
        setBlock(position.toBlockPosition(), block.getBlockId());
    }


    default void setBlock(Position position, short blockId) {
        setBlock(position.toBlockPosition(), blockId);
    }

    default void setCustomBlock(int x, int y, int z, String customBlockId, Data data) {
        CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        setCustomBlock(x, y, z, customBlock.getCustomBlockId(), data);
    }

    default void setCustomBlock(int x, int y, int z, String customBlockId) {
        setCustomBlock(x, y, z, customBlockId, null);
    }

    default void setCustomBlock(BlockPosition blockPosition, String customBlockId) {
        setCustomBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), customBlockId);
    }

    default void setCustomBlock(Position position, String customBlockId) {
        setCustomBlock(position.toBlockPosition(), customBlockId);
    }

}
