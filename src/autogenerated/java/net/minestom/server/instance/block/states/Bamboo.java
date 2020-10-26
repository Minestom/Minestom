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
public final class Bamboo {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9656, "age=0", "leaves=none", "stage=0"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9657, "age=0", "leaves=none", "stage=1"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9658, "age=0", "leaves=small", "stage=0"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9659, "age=0", "leaves=small", "stage=1"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9660, "age=0", "leaves=large", "stage=0"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9661, "age=0", "leaves=large", "stage=1"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9662, "age=1", "leaves=none", "stage=0"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9663, "age=1", "leaves=none", "stage=1"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9664, "age=1", "leaves=small", "stage=0"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9665, "age=1", "leaves=small", "stage=1"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9666, "age=1", "leaves=large", "stage=0"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9667, "age=1", "leaves=large", "stage=1"));
    }
}
