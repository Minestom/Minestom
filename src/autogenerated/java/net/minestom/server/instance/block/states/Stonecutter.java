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
public final class Stonecutter {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.STONECUTTER.addBlockAlternative(new BlockAlternative((short) 15100, "facing=north"));
        Block.STONECUTTER.addBlockAlternative(new BlockAlternative((short) 15101, "facing=south"));
        Block.STONECUTTER.addBlockAlternative(new BlockAlternative((short) 15102, "facing=west"));
        Block.STONECUTTER.addBlockAlternative(new BlockAlternative((short) 15103, "facing=east"));
    }
}
