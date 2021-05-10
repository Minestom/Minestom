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
public final class Barrel {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 14795, "facing=north", "open=true"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 14796, "facing=north", "open=false"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 14797, "facing=east", "open=true"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 14798, "facing=east", "open=false"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 14799, "facing=south", "open=true"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 14800, "facing=south", "open=false"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 14801, "facing=west", "open=true"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 14802, "facing=west", "open=false"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 14803, "facing=up", "open=true"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 14804, "facing=up", "open=false"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 14805, "facing=down", "open=true"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 14806, "facing=down", "open=false"));
    }
}
