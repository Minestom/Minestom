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
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 6839, "facing=north", "triggered=true"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 6840, "facing=north", "triggered=false"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 6841, "facing=east", "triggered=true"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 6842, "facing=east", "triggered=false"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 6843, "facing=south", "triggered=true"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 6844, "facing=south", "triggered=false"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 6845, "facing=west", "triggered=true"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 6846, "facing=west", "triggered=false"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 6847, "facing=up", "triggered=true"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 6848, "facing=up", "triggered=false"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 6849, "facing=down", "triggered=true"));
        Block.DROPPER.addBlockAlternative(new BlockAlternative((short) 6850, "facing=down", "triggered=false"));
    }
}
