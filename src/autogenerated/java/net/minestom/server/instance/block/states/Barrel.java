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
public final class Barrel {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 15041, "facing=north", "open=true"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 15042, "facing=north", "open=false"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 15043, "facing=east", "open=true"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 15044, "facing=east", "open=false"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 15045, "facing=south", "open=true"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 15046, "facing=south", "open=false"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 15047, "facing=west", "open=true"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 15048, "facing=west", "open=false"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 15049, "facing=up", "open=true"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 15050, "facing=up", "open=false"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 15051, "facing=down", "open=true"));
        Block.BARREL.addBlockAlternative(new BlockAlternative((short) 15052, "facing=down", "open=false"));
    }
}
