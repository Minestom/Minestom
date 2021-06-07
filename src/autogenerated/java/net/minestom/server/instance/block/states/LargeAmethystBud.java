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
public final class LargeAmethystBud {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LARGE_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17678, "facing=north", "waterlogged=true"));
        Block.LARGE_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17679, "facing=north", "waterlogged=false"));
        Block.LARGE_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17680, "facing=east", "waterlogged=true"));
        Block.LARGE_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17681, "facing=east", "waterlogged=false"));
        Block.LARGE_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17682, "facing=south", "waterlogged=true"));
        Block.LARGE_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17683, "facing=south", "waterlogged=false"));
        Block.LARGE_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17684, "facing=west", "waterlogged=true"));
        Block.LARGE_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17685, "facing=west", "waterlogged=false"));
        Block.LARGE_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17686, "facing=up", "waterlogged=true"));
        Block.LARGE_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17687, "facing=up", "waterlogged=false"));
        Block.LARGE_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17688, "facing=down", "waterlogged=true"));
        Block.LARGE_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17689, "facing=down", "waterlogged=false"));
    }
}
