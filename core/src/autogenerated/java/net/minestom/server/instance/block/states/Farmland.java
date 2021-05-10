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
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3365, "moisture=0"));
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3366, "moisture=1"));
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3367, "moisture=2"));
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3368, "moisture=3"));
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3369, "moisture=4"));
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3370, "moisture=5"));
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3371, "moisture=6"));
        Block.FARMLAND.addBlockAlternative(new BlockAlternative((short) 3372, "moisture=7"));
    }
}
