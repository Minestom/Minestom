package net.minestom.server.utils.block;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.instance.block.UpdateConsumer;

public class CustomBlockUtils {

    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    /**
     * Get the {@link UpdateConsumer} of a custom block id
     *
     * @param customBlockId the custom block id
     * @return the {@link UpdateConsumer} of the custom block
     */
    public static UpdateConsumer getCustomBlockUpdate(short customBlockId) {
        final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        return getCustomBlockUpdate(customBlock);
    }

    /**
     * Get the {@link UpdateConsumer} of a {@link CustomBlock}
     *
     * @param customBlock the {@link CustomBlock}
     * @return the {@link UpdateConsumer} of the {@link CustomBlock}
     */
    public static UpdateConsumer getCustomBlockUpdate(CustomBlock customBlock) {
        return customBlock != null && customBlock.hasUpdate() ? customBlock::update : null;
    }

}
