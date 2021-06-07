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
public final class WaterCauldron {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WATER_CAULDRON.addBlockAlternative(new BlockAlternative((short) 5343, "level=1"));
        Block.WATER_CAULDRON.addBlockAlternative(new BlockAlternative((short) 5344, "level=2"));
        Block.WATER_CAULDRON.addBlockAlternative(new BlockAlternative((short) 5345, "level=3"));
    }
}
