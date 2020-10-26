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
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6682, "facing=north", "mode=compare", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6683, "facing=north", "mode=compare", "powered=false"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6684, "facing=north", "mode=subtract", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6685, "facing=north", "mode=subtract", "powered=false"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6686, "facing=south", "mode=compare", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6687, "facing=south", "mode=compare", "powered=false"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6688, "facing=south", "mode=subtract", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6689, "facing=south", "mode=subtract", "powered=false"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6690, "facing=west", "mode=compare", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6691, "facing=west", "mode=compare", "powered=false"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6692, "facing=west", "mode=subtract", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6693, "facing=west", "mode=subtract", "powered=false"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6694, "facing=east", "mode=compare", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6695, "facing=east", "mode=compare", "powered=false"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6696, "facing=east", "mode=subtract", "powered=true"));
        Block.COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6697, "facing=east", "mode=subtract", "powered=false"));
    }
}
