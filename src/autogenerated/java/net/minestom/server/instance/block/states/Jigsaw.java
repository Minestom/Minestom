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
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15993, "orientation=down_east"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15994, "orientation=down_north"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15995, "orientation=down_south"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15996, "orientation=down_west"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15997, "orientation=up_east"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15998, "orientation=up_north"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 15999, "orientation=up_south"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 16000, "orientation=up_west"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 16001, "orientation=west_up"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 16002, "orientation=east_up"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 16003, "orientation=north_up"));
        Block.JIGSAW.addBlockAlternative(new BlockAlternative((short) 16004, "orientation=south_up"));
    }
}
