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
public final class Furnace {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.FURNACE.addBlockAlternative(new BlockAlternative((short) 3430, "facing=north", "lit=true"));
        Block.FURNACE.addBlockAlternative(new BlockAlternative((short) 3431, "facing=north", "lit=false"));
        Block.FURNACE.addBlockAlternative(new BlockAlternative((short) 3432, "facing=south", "lit=true"));
        Block.FURNACE.addBlockAlternative(new BlockAlternative((short) 3433, "facing=south", "lit=false"));
        Block.FURNACE.addBlockAlternative(new BlockAlternative((short) 3434, "facing=west", "lit=true"));
        Block.FURNACE.addBlockAlternative(new BlockAlternative((short) 3435, "facing=west", "lit=false"));
        Block.FURNACE.addBlockAlternative(new BlockAlternative((short) 3436, "facing=east", "lit=true"));
        Block.FURNACE.addBlockAlternative(new BlockAlternative((short) 3437, "facing=east", "lit=false"));
    }
}
