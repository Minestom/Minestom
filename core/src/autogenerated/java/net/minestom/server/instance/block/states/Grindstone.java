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
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14825, "face=floor", "facing=north"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14826, "face=floor", "facing=south"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14827, "face=floor", "facing=west"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14828, "face=floor", "facing=east"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14829, "face=wall", "facing=north"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14830, "face=wall", "facing=south"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14831, "face=wall", "facing=west"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14832, "face=wall", "facing=east"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14833, "face=ceiling", "facing=north"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14834, "face=ceiling", "facing=south"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14835, "face=ceiling", "facing=west"));
        Block.GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14836, "face=ceiling", "facing=east"));
    }
}
