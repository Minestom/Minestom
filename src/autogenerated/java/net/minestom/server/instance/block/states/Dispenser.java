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
public final class Dispenser {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 266, "facing=north", "triggered=true"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 267, "facing=north", "triggered=false"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 268, "facing=east", "triggered=true"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 269, "facing=east", "triggered=false"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 270, "facing=south", "triggered=true"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 271, "facing=south", "triggered=false"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 272, "facing=west", "triggered=true"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 273, "facing=west", "triggered=false"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 274, "facing=up", "triggered=true"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 275, "facing=up", "triggered=false"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 276, "facing=down", "triggered=true"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 277, "facing=down", "triggered=false"));
    }
}
