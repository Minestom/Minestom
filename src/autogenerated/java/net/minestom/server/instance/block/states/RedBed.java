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
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1305, "facing=north", "occupied=true", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1306, "facing=north", "occupied=true", "part=foot"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1307, "facing=north", "occupied=false", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1308, "facing=north", "occupied=false", "part=foot"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1309, "facing=south", "occupied=true", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1310, "facing=south", "occupied=true", "part=foot"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1311, "facing=south", "occupied=false", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1312, "facing=south", "occupied=false", "part=foot"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1313, "facing=west", "occupied=true", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1314, "facing=west", "occupied=true", "part=foot"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1315, "facing=west", "occupied=false", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1316, "facing=west", "occupied=false", "part=foot"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1317, "facing=east", "occupied=true", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1318, "facing=east", "occupied=true", "part=foot"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1319, "facing=east", "occupied=false", "part=head"));
        Block.RED_BED.addBlockAlternative(new BlockAlternative((short) 1320, "facing=east", "occupied=false", "part=foot"));
    }
}
