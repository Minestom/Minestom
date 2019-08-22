package fr.themode.minestom.instance;

import fr.themode.minestom.utils.BlockPosition;

public interface BlockModifier {

    void setBlock(int x, int y, int z, short blockId);

    void setBlock(int x, int y, int z, String blockId);

    default void setBlock(BlockPosition blockPosition, short blockId) {
        setBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), blockId);
    }

    default void setBlock(BlockPosition blockPosition, String blockId) {
        setBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), blockId);
    }

}
