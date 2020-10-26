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
public final class PinkBed {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1145, "facing=north", "occupied=true", "part=head"));
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1146, "facing=north", "occupied=true", "part=foot"));
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1147, "facing=north", "occupied=false", "part=head"));
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1148, "facing=north", "occupied=false", "part=foot"));
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1149, "facing=south", "occupied=true", "part=head"));
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1150, "facing=south", "occupied=true", "part=foot"));
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1151, "facing=south", "occupied=false", "part=head"));
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1152, "facing=south", "occupied=false", "part=foot"));
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1153, "facing=west", "occupied=true", "part=head"));
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1154, "facing=west", "occupied=true", "part=foot"));
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1155, "facing=west", "occupied=false", "part=head"));
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1156, "facing=west", "occupied=false", "part=foot"));
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1157, "facing=east", "occupied=true", "part=head"));
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1158, "facing=east", "occupied=true", "part=foot"));
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1159, "facing=east", "occupied=false", "part=head"));
        Block.PINK_BED.addBlockAlternative(new BlockAlternative((short) 1160, "facing=east", "occupied=false", "part=foot"));
    }
}
