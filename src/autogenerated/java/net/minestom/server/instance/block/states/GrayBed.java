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
public final class GrayBed {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1161, "facing=north", "occupied=true", "part=head"));
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1162, "facing=north", "occupied=true", "part=foot"));
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1163, "facing=north", "occupied=false", "part=head"));
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1164, "facing=north", "occupied=false", "part=foot"));
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1165, "facing=south", "occupied=true", "part=head"));
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1166, "facing=south", "occupied=true", "part=foot"));
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1167, "facing=south", "occupied=false", "part=head"));
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1168, "facing=south", "occupied=false", "part=foot"));
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1169, "facing=west", "occupied=true", "part=head"));
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1170, "facing=west", "occupied=true", "part=foot"));
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1171, "facing=west", "occupied=false", "part=head"));
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1172, "facing=west", "occupied=false", "part=foot"));
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1173, "facing=east", "occupied=true", "part=head"));
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1174, "facing=east", "occupied=true", "part=foot"));
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1175, "facing=east", "occupied=false", "part=head"));
        Block.GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1176, "facing=east", "occupied=false", "part=foot"));
    }
}
