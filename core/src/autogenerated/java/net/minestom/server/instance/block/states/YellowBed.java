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
public final class YellowBed {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1113, "facing=north", "occupied=true", "part=head"));
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1114, "facing=north", "occupied=true", "part=foot"));
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1115, "facing=north", "occupied=false", "part=head"));
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1116, "facing=north", "occupied=false", "part=foot"));
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1117, "facing=south", "occupied=true", "part=head"));
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1118, "facing=south", "occupied=true", "part=foot"));
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1119, "facing=south", "occupied=false", "part=head"));
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1120, "facing=south", "occupied=false", "part=foot"));
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1121, "facing=west", "occupied=true", "part=head"));
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1122, "facing=west", "occupied=true", "part=foot"));
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1123, "facing=west", "occupied=false", "part=head"));
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1124, "facing=west", "occupied=false", "part=foot"));
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1125, "facing=east", "occupied=true", "part=head"));
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1126, "facing=east", "occupied=true", "part=foot"));
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1127, "facing=east", "occupied=false", "part=head"));
        Block.YELLOW_BED.addBlockAlternative(new BlockAlternative((short) 1128, "facing=east", "occupied=false", "part=foot"));
    }
}
