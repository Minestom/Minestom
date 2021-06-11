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
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 15053, "facing=north", "lit=true"));
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 15054, "facing=north", "lit=false"));
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 15055, "facing=south", "lit=true"));
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 15056, "facing=south", "lit=false"));
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 15057, "facing=west", "lit=true"));
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 15058, "facing=west", "lit=false"));
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 15059, "facing=east", "lit=true"));
        Block.SMOKER.addBlockAlternative(new BlockAlternative((short) 15060, "facing=east", "lit=false"));
    }
}
