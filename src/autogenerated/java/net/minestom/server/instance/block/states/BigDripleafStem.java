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
public final class BigDripleafStem {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BIG_DRIPLEAF_STEM.addBlockAlternative(new BlockAlternative((short) 18656, "facing=north", "waterlogged=true"));
        Block.BIG_DRIPLEAF_STEM.addBlockAlternative(new BlockAlternative((short) 18657, "facing=north", "waterlogged=false"));
        Block.BIG_DRIPLEAF_STEM.addBlockAlternative(new BlockAlternative((short) 18658, "facing=south", "waterlogged=true"));
        Block.BIG_DRIPLEAF_STEM.addBlockAlternative(new BlockAlternative((short) 18659, "facing=south", "waterlogged=false"));
        Block.BIG_DRIPLEAF_STEM.addBlockAlternative(new BlockAlternative((short) 18660, "facing=west", "waterlogged=true"));
        Block.BIG_DRIPLEAF_STEM.addBlockAlternative(new BlockAlternative((short) 18661, "facing=west", "waterlogged=false"));
        Block.BIG_DRIPLEAF_STEM.addBlockAlternative(new BlockAlternative((short) 18662, "facing=east", "waterlogged=true"));
        Block.BIG_DRIPLEAF_STEM.addBlockAlternative(new BlockAlternative((short) 18663, "facing=east", "waterlogged=false"));
    }
}
