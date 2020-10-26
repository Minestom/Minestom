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
public final class LimeBed {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1129, "facing=north", "occupied=true", "part=head"));
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1130, "facing=north", "occupied=true", "part=foot"));
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1131, "facing=north", "occupied=false", "part=head"));
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1132, "facing=north", "occupied=false", "part=foot"));
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1133, "facing=south", "occupied=true", "part=head"));
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1134, "facing=south", "occupied=true", "part=foot"));
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1135, "facing=south", "occupied=false", "part=head"));
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1136, "facing=south", "occupied=false", "part=foot"));
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1137, "facing=west", "occupied=true", "part=head"));
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1138, "facing=west", "occupied=true", "part=foot"));
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1139, "facing=west", "occupied=false", "part=head"));
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1140, "facing=west", "occupied=false", "part=foot"));
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1141, "facing=east", "occupied=true", "part=head"));
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1142, "facing=east", "occupied=true", "part=foot"));
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1143, "facing=east", "occupied=false", "part=head"));
        Block.LIME_BED.addBlockAlternative(new BlockAlternative((short) 1144, "facing=east", "occupied=false", "part=foot"));
    }
}
