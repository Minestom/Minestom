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
public final class RedBed {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1273, "facing=north", "occupied=true", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1274, "facing=north", "occupied=true", "part=foot"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1275, "facing=north", "occupied=false", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1276, "facing=north", "occupied=false", "part=foot"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1277, "facing=south", "occupied=true", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1278, "facing=south", "occupied=true", "part=foot"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1279, "facing=south", "occupied=false", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1280, "facing=south", "occupied=false", "part=foot"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1281, "facing=west", "occupied=true", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1282, "facing=west", "occupied=true", "part=foot"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1283, "facing=west", "occupied=false", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1284, "facing=west", "occupied=false", "part=foot"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1285, "facing=east", "occupied=true", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1286, "facing=east", "occupied=true", "part=foot"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1287, "facing=east", "occupied=false", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1288, "facing=east", "occupied=false", "part=foot"));
    }
}
