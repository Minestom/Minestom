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
public final class AttachedPumpkinStem {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ATTACHED_PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4837, "facing=north"));
        Block.ATTACHED_PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4838, "facing=south"));
        Block.ATTACHED_PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4839, "facing=west"));
        Block.ATTACHED_PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4840, "facing=east"));
    }
}
