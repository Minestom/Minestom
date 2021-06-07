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
public final class PowderSnowCauldron {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.POWDER_SNOW_CAULDRON.addBlockAlternative(new BlockAlternative((short) 5347, "level=1"));
        Block.POWDER_SNOW_CAULDRON.addBlockAlternative(new BlockAlternative((short) 5348, "level=2"));
        Block.POWDER_SNOW_CAULDRON.addBlockAlternative(new BlockAlternative((short) 5349, "level=3"));
    }
}
