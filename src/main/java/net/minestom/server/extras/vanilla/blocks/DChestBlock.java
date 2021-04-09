package net.minestom.server.extras.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.extras.vanilla.VanillaBlocks;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DChestBlock extends CustomBlock {

    private final short customBlockId;

    public DChestBlock(short defaultBlockStateId, @NotNull String identifier, short customBlockId) {
        super(defaultBlockStateId, identifier);
        this.customBlockId = customBlockId;
    }

    public DChestBlock(@NotNull Block block, @NotNull String identifier, short customBlockId) {
        super(block, identifier);
        this.customBlockId = customBlockId;
    }

    @Override
    public void updateBlockVisual(@NotNull Instance instance, @NotNull Chunk chunk, @NotNull BlockPosition blockPosition, short blockId, @Nullable Data data) {
        int x = blockPosition.getX();
        int y = blockPosition.getY();
        int z = blockPosition.getZ();

        chunk.UNSAFE_setBlock(x, y, z, blockId, customBlockId, data, false);
        VanillaBlocks.sendBlockChange(chunk, blockPosition, blockId);
    }

    @Override
    public void updateBlockVisual(@NotNull Instance instance, @NotNull Chunk chunk, @NotNull Player player, @NotNull BlockFace blockFace, @NotNull BlockPosition blockPosition, short blockId, @Nullable Data data) {
        int x = blockPosition.getX();
        int y = blockPosition.getY();
        int z = blockPosition.getZ();

        // Perhaps add support for different mechanics when sneaking?

        float yaw = (player.getPosition().getYaw() % 360) - 45;
        if (yaw < 0) yaw += 360f;

        byte dir = (byte) (yaw  / (90f));

        switch (blockFace) {
            case TOP:
            case BOTTOM:
                short newBlockId =  blockId;
                switch (dir) {
                    case 0:
                        newBlockId += 18;
                        break;
                    case 1:
                        newBlockId += 6;
                        break;
                    case 2:
                        newBlockId += 12;
                        break;
                    case 3:
                        break;
                }

                chunk.UNSAFE_setBlock(x, y, z, newBlockId, customBlockId, data, false);
                VanillaBlocks.sendBlockChange(chunk, blockPosition, newBlockId);
                break;
            case EAST:
            case WEST:
            case NORTH:
            case SOUTH:
                break;
        }
    }

    @Override
    public void onPlace(@NotNull Instance instance, @NotNull BlockPosition blockPosition, @Nullable Data data) {}

    @Override
    public void onDestroy(@NotNull Instance instance, @NotNull BlockPosition blockPosition, @Nullable Data data) {}

    @Override
    public boolean onInteract(@NotNull Player player, Player.@NotNull Hand hand, @NotNull BlockPosition blockPosition, @Nullable Data data) { return false; }

    @Override
    public short getCustomBlockId() {
        return this.customBlockId;
    }
}
