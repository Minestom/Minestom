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
public final class Grindstone {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 15071, "face=floor", "facing=north"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 15072, "face=floor", "facing=south"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 15073, "face=floor", "facing=west"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 15074, "face=floor", "facing=east"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 15075, "face=wall", "facing=north"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 15076, "face=wall", "facing=south"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 15077, "face=wall", "facing=west"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 15078, "face=wall", "facing=east"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 15079, "face=ceiling", "facing=north"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 15080, "face=ceiling", "facing=south"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 15081, "face=ceiling", "facing=west"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 15082, "face=ceiling", "facing=east"));
    }
}
