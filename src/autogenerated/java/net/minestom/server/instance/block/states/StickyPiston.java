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
public final class StickyPiston {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1385, "extended=true", "facing=north"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1386, "extended=true", "facing=east"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1387, "extended=true", "facing=south"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1388, "extended=true", "facing=west"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1389, "extended=true", "facing=up"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1390, "extended=true", "facing=down"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1391, "extended=false", "facing=north"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1392, "extended=false", "facing=east"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1393, "extended=false", "facing=south"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1394, "extended=false", "facing=west"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1395, "extended=false", "facing=up"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1396, "extended=false", "facing=down"));
    }
}
