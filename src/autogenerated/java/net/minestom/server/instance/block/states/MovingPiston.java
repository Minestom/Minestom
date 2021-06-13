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
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1456, "facing=north", "type=normal"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1457, "facing=north", "type=sticky"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1458, "facing=east", "type=normal"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1459, "facing=east", "type=sticky"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1460, "facing=south", "type=normal"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1461, "facing=south", "type=sticky"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1462, "facing=west", "type=normal"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1463, "facing=west", "type=sticky"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1464, "facing=up", "type=normal"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1465, "facing=up", "type=sticky"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1466, "facing=down", "type=normal"));
        Block.MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1467, "facing=down", "type=sticky"));
    }
}
