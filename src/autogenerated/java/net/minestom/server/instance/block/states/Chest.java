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
public final class Chest {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2090, "facing=north", "type=single", "waterlogged=true"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2091, "facing=north", "type=single", "waterlogged=false"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2092, "facing=north", "type=left", "waterlogged=true"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2093, "facing=north", "type=left", "waterlogged=false"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2094, "facing=north", "type=right", "waterlogged=true"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2095, "facing=north", "type=right", "waterlogged=false"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2096, "facing=south", "type=single", "waterlogged=true"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2097, "facing=south", "type=single", "waterlogged=false"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2098, "facing=south", "type=left", "waterlogged=true"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2099, "facing=south", "type=left", "waterlogged=false"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2100, "facing=south", "type=right", "waterlogged=true"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2101, "facing=south", "type=right", "waterlogged=false"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2102, "facing=west", "type=single", "waterlogged=true"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2103, "facing=west", "type=single", "waterlogged=false"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2104, "facing=west", "type=left", "waterlogged=true"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2105, "facing=west", "type=left", "waterlogged=false"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2106, "facing=west", "type=right", "waterlogged=true"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2107, "facing=west", "type=right", "waterlogged=false"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2108, "facing=east", "type=single", "waterlogged=true"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2109, "facing=east", "type=single", "waterlogged=false"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2110, "facing=east", "type=left", "waterlogged=true"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2111, "facing=east", "type=left", "waterlogged=false"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2112, "facing=east", "type=right", "waterlogged=true"));
        Block.CHEST.addBlockAlternative(new BlockAlternative((short) 2113, "facing=east", "type=right", "waterlogged=false"));
    }
}
