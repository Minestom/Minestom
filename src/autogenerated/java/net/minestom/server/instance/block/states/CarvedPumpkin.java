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
        Block.CARVED_PUMPKIN.addBlockAlternative(new BlockAlternative((short) 4085, "facing=north"));
        Block.CARVED_PUMPKIN.addBlockAlternative(new BlockAlternative((short) 4086, "facing=south"));
        Block.CARVED_PUMPKIN.addBlockAlternative(new BlockAlternative((short) 4087, "facing=west"));
        Block.CARVED_PUMPKIN.addBlockAlternative(new BlockAlternative((short) 4088, "facing=east"));
    }
}
