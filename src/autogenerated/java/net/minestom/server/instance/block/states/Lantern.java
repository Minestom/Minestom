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
public final class Lantern {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LANTERN.addBlockAlternative(new BlockAlternative((short) 14890, "hanging=true", "waterlogged=true"));
        Block.LANTERN.addBlockAlternative(new BlockAlternative((short) 14891, "hanging=true", "waterlogged=false"));
        Block.LANTERN.addBlockAlternative(new BlockAlternative((short) 14892, "hanging=false", "waterlogged=true"));
        Block.LANTERN.addBlockAlternative(new BlockAlternative((short) 14893, "hanging=false", "waterlogged=false"));
    }
}
