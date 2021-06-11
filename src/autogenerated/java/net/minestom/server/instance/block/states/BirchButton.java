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
public final class BirchButton {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6600, "face=floor", "facing=north", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6601, "face=floor", "facing=north", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6602, "face=floor", "facing=south", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6603, "face=floor", "facing=south", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6604, "face=floor", "facing=west", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6605, "face=floor", "facing=west", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6606, "face=floor", "facing=east", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6607, "face=floor", "facing=east", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6608, "face=wall", "facing=north", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6609, "face=wall", "facing=north", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6610, "face=wall", "facing=south", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6611, "face=wall", "facing=south", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6612, "face=wall", "facing=west", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6613, "face=wall", "facing=west", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6614, "face=wall", "facing=east", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6615, "face=wall", "facing=east", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6616, "face=ceiling", "facing=north", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6617, "face=ceiling", "facing=north", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6618, "face=ceiling", "facing=south", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6619, "face=ceiling", "facing=south", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6620, "face=ceiling", "facing=west", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6621, "face=ceiling", "facing=west", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6622, "face=ceiling", "facing=east", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6623, "face=ceiling", "facing=east", "powered=false"));
    }
}
