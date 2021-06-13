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
public final class SmallDripleaf {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18664, "facing=north", "half=upper", "waterlogged=true"));
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18665, "facing=north", "half=upper", "waterlogged=false"));
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18666, "facing=north", "half=lower", "waterlogged=true"));
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18667, "facing=north", "half=lower", "waterlogged=false"));
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18668, "facing=south", "half=upper", "waterlogged=true"));
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18669, "facing=south", "half=upper", "waterlogged=false"));
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18670, "facing=south", "half=lower", "waterlogged=true"));
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18671, "facing=south", "half=lower", "waterlogged=false"));
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18672, "facing=west", "half=upper", "waterlogged=true"));
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18673, "facing=west", "half=upper", "waterlogged=false"));
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18674, "facing=west", "half=lower", "waterlogged=true"));
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18675, "facing=west", "half=lower", "waterlogged=false"));
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18676, "facing=east", "half=upper", "waterlogged=true"));
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18677, "facing=east", "half=upper", "waterlogged=false"));
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18678, "facing=east", "half=lower", "waterlogged=true"));
        Block.SMALL_DRIPLEAF.addBlockAlternative(new BlockAlternative((short) 18679, "facing=east", "half=lower", "waterlogged=false"));
    }
}
