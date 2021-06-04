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
public final class CarvedPumpkin {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CARVED_PUMPKIN.addBlockAlternative(new BlockAlternative((short) 4016, "facing=north"));
        Block.CARVED_PUMPKIN.addBlockAlternative(new BlockAlternative((short) 4017, "facing=south"));
        Block.CARVED_PUMPKIN.addBlockAlternative(new BlockAlternative((short) 4018, "facing=west"));
        Block.CARVED_PUMPKIN.addBlockAlternative(new BlockAlternative((short) 4019, "facing=east"));
    }
}
