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
public final class PistonHead {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1416, "facing=north", "short=true", "type=normal"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1417, "facing=north", "short=true", "type=sticky"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1418, "facing=north", "short=false", "type=normal"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1419, "facing=north", "short=false", "type=sticky"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1420, "facing=east", "short=true", "type=normal"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1421, "facing=east", "short=true", "type=sticky"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1422, "facing=east", "short=false", "type=normal"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1423, "facing=east", "short=false", "type=sticky"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1424, "facing=south", "short=true", "type=normal"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1425, "facing=south", "short=true", "type=sticky"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1426, "facing=south", "short=false", "type=normal"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1427, "facing=south", "short=false", "type=sticky"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1428, "facing=west", "short=true", "type=normal"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1429, "facing=west", "short=true", "type=sticky"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1430, "facing=west", "short=false", "type=normal"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1431, "facing=west", "short=false", "type=sticky"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1432, "facing=up", "short=true", "type=normal"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1433, "facing=up", "short=true", "type=sticky"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1434, "facing=up", "short=false", "type=normal"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1435, "facing=up", "short=false", "type=sticky"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1436, "facing=down", "short=true", "type=normal"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1437, "facing=down", "short=true", "type=sticky"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1438, "facing=down", "short=false", "type=normal"));
        Block.PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1439, "facing=down", "short=false", "type=sticky"));
    }
}
