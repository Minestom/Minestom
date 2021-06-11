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
public final class SmallAmethystBud {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SMALL_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17702, "facing=north", "waterlogged=true"));
        Block.SMALL_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17703, "facing=north", "waterlogged=false"));
        Block.SMALL_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17704, "facing=east", "waterlogged=true"));
        Block.SMALL_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17705, "facing=east", "waterlogged=false"));
        Block.SMALL_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17706, "facing=south", "waterlogged=true"));
        Block.SMALL_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17707, "facing=south", "waterlogged=false"));
        Block.SMALL_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17708, "facing=west", "waterlogged=true"));
        Block.SMALL_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17709, "facing=west", "waterlogged=false"));
        Block.SMALL_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17710, "facing=up", "waterlogged=true"));
        Block.SMALL_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17711, "facing=up", "waterlogged=false"));
        Block.SMALL_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17712, "facing=down", "waterlogged=true"));
        Block.SMALL_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17713, "facing=down", "waterlogged=false"));
    }
}
