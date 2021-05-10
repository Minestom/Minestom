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
public final class MagentaBed {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1081, "facing=north", "occupied=true", "part=head"));
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1082, "facing=north", "occupied=true", "part=foot"));
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1083, "facing=north", "occupied=false", "part=head"));
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1084, "facing=north", "occupied=false", "part=foot"));
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1085, "facing=south", "occupied=true", "part=head"));
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1086, "facing=south", "occupied=true", "part=foot"));
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1087, "facing=south", "occupied=false", "part=head"));
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1088, "facing=south", "occupied=false", "part=foot"));
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1089, "facing=west", "occupied=true", "part=head"));
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1090, "facing=west", "occupied=true", "part=foot"));
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1091, "facing=west", "occupied=false", "part=head"));
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1092, "facing=west", "occupied=false", "part=foot"));
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1093, "facing=east", "occupied=true", "part=head"));
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1094, "facing=east", "occupied=true", "part=foot"));
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1095, "facing=east", "occupied=false", "part=head"));
        Block.MAGENTA_BED.addBlockAlternative(new BlockAlternative((short) 1096, "facing=east", "occupied=false", "part=foot"));
    }
}
