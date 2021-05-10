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
public final class MovingPiston {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1400, "facing=north", "type=normal"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1401, "facing=north", "type=sticky"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1402, "facing=east", "type=normal"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1403, "facing=east", "type=sticky"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1404, "facing=south", "type=normal"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1405, "facing=south", "type=sticky"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1406, "facing=west", "type=normal"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1407, "facing=west", "type=sticky"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1408, "facing=up", "type=normal"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1409, "facing=up", "type=sticky"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1410, "facing=down", "type=normal"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1411, "facing=down", "type=sticky"));
    }
}
