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
public final class ChorusFlower {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 9378, "age=0"));
        Block.CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 9379, "age=1"));
        Block.CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 9380, "age=2"));
        Block.CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 9381, "age=3"));
        Block.CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 9382, "age=4"));
        Block.CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 9383, "age=5"));
    }
}
