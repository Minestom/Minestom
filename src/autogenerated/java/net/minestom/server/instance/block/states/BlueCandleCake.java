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
public final class BlueCandleCake {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BLUE_CANDLE_CAKE.addBlockAlternative(new BlockAlternative((short) 17654, "lit=true"));
        Block.BLUE_CANDLE_CAKE.addBlockAlternative(new BlockAlternative((short) 17655, "lit=false"));
    }
}
