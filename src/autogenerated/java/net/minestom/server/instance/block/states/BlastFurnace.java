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
public final class BlastFurnace {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 15061, "facing=north", "lit=true"));
        Block.BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 15062, "facing=north", "lit=false"));
        Block.BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 15063, "facing=south", "lit=true"));
        Block.BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 15064, "facing=south", "lit=false"));
        Block.BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 15065, "facing=west", "lit=true"));
        Block.BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 15066, "facing=west", "lit=false"));
        Block.BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 15067, "facing=east", "lit=true"));
        Block.BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 15068, "facing=east", "lit=false"));
    }
}
