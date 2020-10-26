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
public final class Jigsaw {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15747, "orientation=down_east"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15748, "orientation=down_north"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15749, "orientation=down_south"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15750, "orientation=down_west"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15751, "orientation=up_east"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15752, "orientation=up_north"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15753, "orientation=up_south"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15754, "orientation=up_west"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15755, "orientation=west_up"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15756, "orientation=east_up"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15757, "orientation=north_up"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15758, "orientation=south_up"));
    }
}
