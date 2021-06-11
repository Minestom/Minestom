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
public final class TrappedChest {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6828, "facing=north", "type=single", "waterlogged=true"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6829, "facing=north", "type=single", "waterlogged=false"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6830, "facing=north", "type=left", "waterlogged=true"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6831, "facing=north", "type=left", "waterlogged=false"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6832, "facing=north", "type=right", "waterlogged=true"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6833, "facing=north", "type=right", "waterlogged=false"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6834, "facing=south", "type=single", "waterlogged=true"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6835, "facing=south", "type=single", "waterlogged=false"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6836, "facing=south", "type=left", "waterlogged=true"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6837, "facing=south", "type=left", "waterlogged=false"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6838, "facing=south", "type=right", "waterlogged=true"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6839, "facing=south", "type=right", "waterlogged=false"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6840, "facing=west", "type=single", "waterlogged=true"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6841, "facing=west", "type=single", "waterlogged=false"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6842, "facing=west", "type=left", "waterlogged=true"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6843, "facing=west", "type=left", "waterlogged=false"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6844, "facing=west", "type=right", "waterlogged=true"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6845, "facing=west", "type=right", "waterlogged=false"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6846, "facing=east", "type=single", "waterlogged=true"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6847, "facing=east", "type=single", "waterlogged=false"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6848, "facing=east", "type=left", "waterlogged=true"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6849, "facing=east", "type=left", "waterlogged=false"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6850, "facing=east", "type=right", "waterlogged=true"));
        Block.TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6851, "facing=east", "type=right", "waterlogged=false"));
    }
}
