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
public final class LightGrayBed {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1177, "facing=north", "occupied=true", "part=head"));
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1178, "facing=north", "occupied=true", "part=foot"));
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1179, "facing=north", "occupied=false", "part=head"));
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1180, "facing=north", "occupied=false", "part=foot"));
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1181, "facing=south", "occupied=true", "part=head"));
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1182, "facing=south", "occupied=true", "part=foot"));
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1183, "facing=south", "occupied=false", "part=head"));
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1184, "facing=south", "occupied=false", "part=foot"));
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1185, "facing=west", "occupied=true", "part=head"));
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1186, "facing=west", "occupied=true", "part=foot"));
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1187, "facing=west", "occupied=false", "part=head"));
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1188, "facing=west", "occupied=false", "part=foot"));
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1189, "facing=east", "occupied=true", "part=head"));
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1190, "facing=east", "occupied=true", "part=foot"));
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1191, "facing=east", "occupied=false", "part=head"));
        Block.LIGHT_GRAY_BED.addBlockAlternative(new BlockAlternative((short) 1192, "facing=east", "occupied=false", "part=foot"));
    }
}
