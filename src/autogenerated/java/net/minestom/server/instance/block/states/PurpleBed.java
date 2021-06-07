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
public final class PurpleBed {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1241, "facing=north", "occupied=true", "part=head"));
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1242, "facing=north", "occupied=true", "part=foot"));
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1243, "facing=north", "occupied=false", "part=head"));
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1244, "facing=north", "occupied=false", "part=foot"));
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1245, "facing=south", "occupied=true", "part=head"));
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1246, "facing=south", "occupied=true", "part=foot"));
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1247, "facing=south", "occupied=false", "part=head"));
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1248, "facing=south", "occupied=false", "part=foot"));
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1249, "facing=west", "occupied=true", "part=head"));
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1250, "facing=west", "occupied=true", "part=foot"));
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1251, "facing=west", "occupied=false", "part=head"));
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1252, "facing=west", "occupied=false", "part=foot"));
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1253, "facing=east", "occupied=true", "part=head"));
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1254, "facing=east", "occupied=true", "part=foot"));
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1255, "facing=east", "occupied=false", "part=head"));
        Block.PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1256, "facing=east", "occupied=false", "part=foot"));
    }
}
