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
public final class AttachedMelonStem {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ATTACHED_MELON_STEM.addBlockAlternative(new BlockAlternative((short) 4841, "facing=north"));
        Block.ATTACHED_MELON_STEM.addBlockAlternative(new BlockAlternative((short) 4842, "facing=south"));
        Block.ATTACHED_MELON_STEM.addBlockAlternative(new BlockAlternative((short) 4843, "facing=west"));
        Block.ATTACHED_MELON_STEM.addBlockAlternative(new BlockAlternative((short) 4844, "facing=east"));
    }
}
