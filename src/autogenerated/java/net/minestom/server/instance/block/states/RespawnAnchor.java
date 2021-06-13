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
public final class RespawnAnchor {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.RESPAWN_ANCHOR.addBlockAlternative(new BlockAlternative((short) 16083, "charges=0"));
        Block.RESPAWN_ANCHOR.addBlockAlternative(new BlockAlternative((short) 16084, "charges=1"));
        Block.RESPAWN_ANCHOR.addBlockAlternative(new BlockAlternative((short) 16085, "charges=2"));
        Block.RESPAWN_ANCHOR.addBlockAlternative(new BlockAlternative((short) 16086, "charges=3"));
        Block.RESPAWN_ANCHOR.addBlockAlternative(new BlockAlternative((short) 16087, "charges=4"));
    }
}
