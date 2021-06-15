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
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5457, "facing=north", "waterlogged=true"));
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5458, "facing=north", "waterlogged=false"));
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5459, "facing=south", "waterlogged=true"));
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5460, "facing=south", "waterlogged=false"));
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5461, "facing=west", "waterlogged=true"));
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5462, "facing=west", "waterlogged=false"));
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5463, "facing=east", "waterlogged=true"));
        Block.ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5464, "facing=east", "waterlogged=false"));
    }
}
