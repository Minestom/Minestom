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
public final class Dropper {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 7053, "facing=north", "triggered=true"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 7054, "facing=north", "triggered=false"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 7055, "facing=east", "triggered=true"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 7056, "facing=east", "triggered=false"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 7057, "facing=south", "triggered=true"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 7058, "facing=south", "triggered=false"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 7059, "facing=west", "triggered=true"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 7060, "facing=west", "triggered=false"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 7061, "facing=up", "triggered=true"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 7062, "facing=up", "triggered=false"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 7063, "facing=down", "triggered=true"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 7064, "facing=down", "triggered=false"));
    }
}
