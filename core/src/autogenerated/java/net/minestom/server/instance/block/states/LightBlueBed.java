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
public final class LightBlueBed {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1097, "facing=north", "occupied=true", "part=head"));
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1098, "facing=north", "occupied=true", "part=foot"));
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1099, "facing=north", "occupied=false", "part=head"));
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1100, "facing=north", "occupied=false", "part=foot"));
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1101, "facing=south", "occupied=true", "part=head"));
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1102, "facing=south", "occupied=true", "part=foot"));
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1103, "facing=south", "occupied=false", "part=head"));
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1104, "facing=south", "occupied=false", "part=foot"));
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1105, "facing=west", "occupied=true", "part=head"));
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1106, "facing=west", "occupied=true", "part=foot"));
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1107, "facing=west", "occupied=false", "part=head"));
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1108, "facing=west", "occupied=false", "part=foot"));
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1109, "facing=east", "occupied=true", "part=head"));
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1110, "facing=east", "occupied=true", "part=foot"));
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1111, "facing=east", "occupied=false", "part=head"));
        Block.LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1112, "facing=east", "occupied=false", "part=foot"));
    }
}
