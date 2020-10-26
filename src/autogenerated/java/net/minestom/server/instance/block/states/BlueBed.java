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
public final class BlueBed {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1225, "facing=north", "occupied=true", "part=head"));
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1226, "facing=north", "occupied=true", "part=foot"));
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1227, "facing=north", "occupied=false", "part=head"));
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1228, "facing=north", "occupied=false", "part=foot"));
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1229, "facing=south", "occupied=true", "part=head"));
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1230, "facing=south", "occupied=true", "part=foot"));
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1231, "facing=south", "occupied=false", "part=head"));
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1232, "facing=south", "occupied=false", "part=foot"));
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1233, "facing=west", "occupied=true", "part=head"));
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1234, "facing=west", "occupied=true", "part=foot"));
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1235, "facing=west", "occupied=false", "part=head"));
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1236, "facing=west", "occupied=false", "part=foot"));
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1237, "facing=east", "occupied=true", "part=head"));
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1238, "facing=east", "occupied=true", "part=foot"));
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1239, "facing=east", "occupied=false", "part=head"));
        Block.BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1240, "facing=east", "occupied=false", "part=foot"));
    }
}
