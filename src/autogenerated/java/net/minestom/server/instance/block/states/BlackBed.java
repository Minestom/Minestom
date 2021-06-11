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
public final class BlackBed {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1321, "facing=north", "occupied=true", "part=head"));
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1322, "facing=north", "occupied=true", "part=foot"));
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1323, "facing=north", "occupied=false", "part=head"));
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1324, "facing=north", "occupied=false", "part=foot"));
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1325, "facing=south", "occupied=true", "part=head"));
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1326, "facing=south", "occupied=true", "part=foot"));
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1327, "facing=south", "occupied=false", "part=head"));
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1328, "facing=south", "occupied=false", "part=foot"));
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1329, "facing=west", "occupied=true", "part=head"));
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1330, "facing=west", "occupied=true", "part=foot"));
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1331, "facing=west", "occupied=false", "part=head"));
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1332, "facing=west", "occupied=false", "part=foot"));
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1333, "facing=east", "occupied=true", "part=head"));
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1334, "facing=east", "occupied=true", "part=foot"));
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1335, "facing=east", "occupied=false", "part=head"));
        Block.BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1336, "facing=east", "occupied=false", "part=foot"));
    }
}
