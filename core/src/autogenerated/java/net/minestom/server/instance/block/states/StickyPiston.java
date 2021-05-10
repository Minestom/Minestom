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
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1329, "extended=true", "facing=north"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1330, "extended=true", "facing=east"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1331, "extended=true", "facing=south"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1332, "extended=true", "facing=west"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1333, "extended=true", "facing=up"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1334, "extended=true", "facing=down"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1335, "extended=false", "facing=north"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1336, "extended=false", "facing=east"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1337, "extended=false", "facing=south"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1338, "extended=false", "facing=west"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1339, "extended=false", "facing=up"));
        Block.STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1340, "extended=false", "facing=down"));
    }
}
