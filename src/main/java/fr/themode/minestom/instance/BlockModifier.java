package fr.themode.minestom.instance;

import fr.themode.minestom.Main;
import fr.themode.minestom.instance.block.BlockManager;
import fr.themode.minestom.instance.block.CustomBlock;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Position;

public interface BlockModifier {

    BlockManager BLOCK_MANAGER = Main.getBlockManager();

    void setBlock(int x, int y, int z, short blockId);

    void setCustomBlock(int x, int y, int z, short blockId);

    default void setBlock(BlockPosition blockPosition, short blockId) {
        setBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), blockId);
    }

    default void setBlock(Position position, short blockId) {
        setBlock(position.toBlockPosition(), blockId);
    }

    default void setCustomBlock(int x, int y, int z, String blockId) {
        CustomBlock customBlock = BLOCK_MANAGER.getBlock(blockId);
        setCustomBlock(x, y, z, customBlock.getId());
    }

    default void setCustomBlock(BlockPosition blockPosition, String blockId) {
        setCustomBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), blockId);
    }

    default void setCustomBlock(Position position, String blockId) {
        setCustomBlock(position.toBlockPosition(), blockId);
    }

}
