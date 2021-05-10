package net.minestom.server.utils.block;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;

public class CustomBlockUtils {

    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    /**
     * Gets if a custom block id has an update method.
     *
     * @param customBlockId the custom block id
     * @return true if <code>customBlockId</code> has an update method
     */
    public static boolean hasUpdate(short customBlockId) {
        final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        return hasUpdate(customBlock);
    }

    /**
     * Gets if a {@link CustomBlock} has an update method.
     *
     * @param customBlock the {@link CustomBlock}
     * @return true if <code>customBlock</code> has an update method
     */
    public static boolean hasUpdate(CustomBlock customBlock) {
        return customBlock != null && customBlock.hasUpdate();
    }

}
