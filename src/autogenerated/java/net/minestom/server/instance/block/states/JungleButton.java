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
public final class JungleButton {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6624, "face=floor", "facing=north", "powered=true"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6625, "face=floor", "facing=north", "powered=false"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6626, "face=floor", "facing=south", "powered=true"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6627, "face=floor", "facing=south", "powered=false"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6628, "face=floor", "facing=west", "powered=true"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6629, "face=floor", "facing=west", "powered=false"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6630, "face=floor", "facing=east", "powered=true"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6631, "face=floor", "facing=east", "powered=false"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6632, "face=wall", "facing=north", "powered=true"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6633, "face=wall", "facing=north", "powered=false"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6634, "face=wall", "facing=south", "powered=true"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6635, "face=wall", "facing=south", "powered=false"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6636, "face=wall", "facing=west", "powered=true"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6637, "face=wall", "facing=west", "powered=false"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6638, "face=wall", "facing=east", "powered=true"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6639, "face=wall", "facing=east", "powered=false"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6640, "face=ceiling", "facing=north", "powered=true"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6641, "face=ceiling", "facing=north", "powered=false"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6642, "face=ceiling", "facing=south", "powered=true"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6643, "face=ceiling", "facing=south", "powered=false"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6644, "face=ceiling", "facing=west", "powered=true"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6645, "face=ceiling", "facing=west", "powered=false"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6646, "face=ceiling", "facing=east", "powered=true"));
        Block.JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6647, "face=ceiling", "facing=east", "powered=false"));
    }
}
