package fr.themode.minestom.instance;

import fr.themode.minestom.MinecraftServer;
import fr.themode.minestom.data.Data;
import fr.themode.minestom.instance.block.Block;
import fr.themode.minestom.instance.block.BlockManager;
import fr.themode.minestom.instance.block.CustomBlock;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Position;

public interface BlockModifier {

    BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    void setBlock(int x, int y, int z, short blockId, Data data);

    void setCustomBlock(int x, int y, int z, short blockId, Data data);

    default void setBlock(int x, int y, int z, short blockId) {
        setBlock(x, y, z, blockId, null);
    }

    default void setBlock(int x, int y, int z, Block block) {
        setBlock(x, y, z, block.getBlockId(), null);
    }

    default void setCustomBlock(int x, int y, int z, short blockId) {
        setCustomBlock(x, y, z, blockId, null);
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

    default void setCustomBlock(int x, int y, int z, String blockId, Data data) {
        CustomBlock customBlock = BLOCK_MANAGER.getBlock(blockId);
        setCustomBlock(x, y, z, customBlock.getId(), data);
    }

    default void setCustomBlock(int x, int y, int z, String blockId) {
        setCustomBlock(x, y, z, blockId, null);
    }

    default void setCustomBlock(BlockPosition blockPosition, String blockId) {
        setCustomBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), blockId);
    }

    default void setCustomBlock(Position position, String blockId) {
        setCustomBlock(position.toBlockPosition(), blockId);
    }

}
