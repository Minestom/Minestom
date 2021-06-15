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
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9902, "age=0", "leaves=none", "stage=0"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9903, "age=0", "leaves=none", "stage=1"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9904, "age=0", "leaves=small", "stage=0"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9905, "age=0", "leaves=small", "stage=1"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9906, "age=0", "leaves=large", "stage=0"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9907, "age=0", "leaves=large", "stage=1"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9908, "age=1", "leaves=none", "stage=0"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9909, "age=1", "leaves=none", "stage=1"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9910, "age=1", "leaves=small", "stage=0"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9911, "age=1", "leaves=small", "stage=1"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9912, "age=1", "leaves=large", "stage=0"));
        Block.BAMBOO.addBlockAlternative(new BlockAlternative((short) 9913, "age=1", "leaves=large", "stage=1"));
    }
}
