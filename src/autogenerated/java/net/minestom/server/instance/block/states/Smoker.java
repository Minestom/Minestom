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
public final class Smoker {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 14807, "facing=north", "lit=true"));
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 14808, "facing=north", "lit=false"));
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 14809, "facing=south", "lit=true"));
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 14810, "facing=south", "lit=false"));
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 14811, "facing=west", "lit=true"));
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 14812, "facing=west", "lit=false"));
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 14813, "facing=east", "lit=true"));
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 14814, "facing=east", "lit=false"));
    }
}
