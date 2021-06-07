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
        Block.CAKE.addBlockAlternative(new BlockAlternative((short) 4093, "bites=0"));
        Block.CAKE.addBlockAlternative(new BlockAlternative((short) 4094, "bites=1"));
        Block.CAKE.addBlockAlternative(new BlockAlternative((short) 4095, "bites=2"));
        Block.CAKE.addBlockAlternative(new BlockAlternative((short) 4096, "bites=3"));
        Block.CAKE.addBlockAlternative(new BlockAlternative((short) 4097, "bites=4"));
        Block.CAKE.addBlockAlternative(new BlockAlternative((short) 4098, "bites=5"));
        Block.CAKE.addBlockAlternative(new BlockAlternative((short) 4099, "bites=6"));
    }
}
