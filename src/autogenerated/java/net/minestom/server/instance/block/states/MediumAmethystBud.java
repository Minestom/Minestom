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
public final class MediumAmethystBud {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.MEDIUM_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17690, "facing=north", "waterlogged=true"));
        Block.MEDIUM_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17691, "facing=north", "waterlogged=false"));
        Block.MEDIUM_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17692, "facing=east", "waterlogged=true"));
        Block.MEDIUM_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17693, "facing=east", "waterlogged=false"));
        Block.MEDIUM_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17694, "facing=south", "waterlogged=true"));
        Block.MEDIUM_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17695, "facing=south", "waterlogged=false"));
        Block.MEDIUM_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17696, "facing=west", "waterlogged=true"));
        Block.MEDIUM_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17697, "facing=west", "waterlogged=false"));
        Block.MEDIUM_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17698, "facing=up", "waterlogged=true"));
        Block.MEDIUM_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17699, "facing=up", "waterlogged=false"));
        Block.MEDIUM_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17700, "facing=down", "waterlogged=true"));
        Block.MEDIUM_AMETHYST_BUD.addBlockAlternative(new BlockAlternative((short) 17701, "facing=down", "waterlogged=false"));
    }
}
