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
public final class CyanBed {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1193, "facing=north", "occupied=true", "part=head"));
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1194, "facing=north", "occupied=true", "part=foot"));
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1195, "facing=north", "occupied=false", "part=head"));
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1196, "facing=north", "occupied=false", "part=foot"));
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1197, "facing=south", "occupied=true", "part=head"));
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1198, "facing=south", "occupied=true", "part=foot"));
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1199, "facing=south", "occupied=false", "part=head"));
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1200, "facing=south", "occupied=false", "part=foot"));
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1201, "facing=west", "occupied=true", "part=head"));
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1202, "facing=west", "occupied=true", "part=foot"));
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1203, "facing=west", "occupied=false", "part=head"));
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1204, "facing=west", "occupied=false", "part=foot"));
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1205, "facing=east", "occupied=true", "part=head"));
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1206, "facing=east", "occupied=true", "part=foot"));
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1207, "facing=east", "occupied=false", "part=head"));
        Block.CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1208, "facing=east", "occupied=false", "part=foot"));
    }
}
