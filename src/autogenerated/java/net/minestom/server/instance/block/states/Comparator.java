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
public final class Comparator {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6884, "facing=north", "mode=compare", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6885, "facing=north", "mode=compare", "powered=false"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6886, "facing=north", "mode=subtract", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6887, "facing=north", "mode=subtract", "powered=false"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6888, "facing=south", "mode=compare", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6889, "facing=south", "mode=compare", "powered=false"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6890, "facing=south", "mode=subtract", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6891, "facing=south", "mode=subtract", "powered=false"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6892, "facing=west", "mode=compare", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6893, "facing=west", "mode=compare", "powered=false"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6894, "facing=west", "mode=subtract", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6895, "facing=west", "mode=subtract", "powered=false"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6896, "facing=east", "mode=compare", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6897, "facing=east", "mode=compare", "powered=false"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6898, "facing=east", "mode=subtract", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6899, "facing=east", "mode=subtract", "powered=false"));
    }
}
