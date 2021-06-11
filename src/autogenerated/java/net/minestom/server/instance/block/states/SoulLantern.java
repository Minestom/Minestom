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
public final class SoulLantern {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SOUL_LANTERN.addBlockAlternative(new BlockAlternative((short) 15140, "hanging=true", "waterlogged=true"));
        Block.SOUL_LANTERN.addBlockAlternative(new BlockAlternative((short) 15141, "hanging=true", "waterlogged=false"));
        Block.SOUL_LANTERN.addBlockAlternative(new BlockAlternative((short) 15142, "hanging=false", "waterlogged=true"));
        Block.SOUL_LANTERN.addBlockAlternative(new BlockAlternative((short) 15143, "hanging=false", "waterlogged=false"));
    }
}
