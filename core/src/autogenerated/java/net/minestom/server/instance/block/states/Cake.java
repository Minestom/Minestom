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
public final class Cake {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CAKE.addBlockAlternative(new BlockAlternative((short) 4024, "bites=0"));
        Block.CAKE.addBlockAlternative(new BlockAlternative((short) 4025, "bites=1"));
        Block.CAKE.addBlockAlternative(new BlockAlternative((short) 4026, "bites=2"));
        Block.CAKE.addBlockAlternative(new BlockAlternative((short) 4027, "bites=3"));
        Block.CAKE.addBlockAlternative(new BlockAlternative((short) 4028, "bites=4"));
        Block.CAKE.addBlockAlternative(new BlockAlternative((short) 4029, "bites=5"));
        Block.CAKE.addBlockAlternative(new BlockAlternative((short) 4030, "bites=6"));
    }
}
