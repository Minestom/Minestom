package net.minestom.server.instance.block.states;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockAlternative;

/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(
        since = "forever",
        forRemoval = false
)
public final class EnderChest {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5255, "facing=north", "waterlogged=true"));
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5256, "facing=north", "waterlogged=false"));
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5257, "facing=south", "waterlogged=true"));
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5258, "facing=south", "waterlogged=false"));
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5259, "facing=west", "waterlogged=true"));
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5260, "facing=west", "waterlogged=false"));
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5261, "facing=east", "waterlogged=true"));
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5262, "facing=east", "waterlogged=false"));
    }
}
