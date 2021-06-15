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
public final class Farmland {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3422, "moisture=0"));
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3423, "moisture=1"));
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3424, "moisture=2"));
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3425, "moisture=3"));
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3426, "moisture=4"));
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3427, "moisture=5"));
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3428, "moisture=6"));
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3429, "moisture=7"));
    }
}
