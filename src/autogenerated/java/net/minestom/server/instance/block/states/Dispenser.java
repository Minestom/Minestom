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
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 234, "facing=north", "triggered=true"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 235, "facing=north", "triggered=false"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 236, "facing=east", "triggered=true"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 237, "facing=east", "triggered=false"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 238, "facing=south", "triggered=true"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 239, "facing=south", "triggered=false"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 240, "facing=west", "triggered=true"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 241, "facing=west", "triggered=false"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 242, "facing=up", "triggered=true"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 243, "facing=up", "triggered=false"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 244, "facing=down", "triggered=true"));
        Block.DISPENSER.addBlockAlternative(new BlockAlternative((short) 245, "facing=down", "triggered=false"));
    }
}
