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
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6398, "face=floor", "facing=north", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6399, "face=floor", "facing=north", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6400, "face=floor", "facing=south", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6401, "face=floor", "facing=south", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6402, "face=floor", "facing=west", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6403, "face=floor", "facing=west", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6404, "face=floor", "facing=east", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6405, "face=floor", "facing=east", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6406, "face=wall", "facing=north", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6407, "face=wall", "facing=north", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6408, "face=wall", "facing=south", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6409, "face=wall", "facing=south", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6410, "face=wall", "facing=west", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6411, "face=wall", "facing=west", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6412, "face=wall", "facing=east", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6413, "face=wall", "facing=east", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6414, "face=ceiling", "facing=north", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6415, "face=ceiling", "facing=north", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6416, "face=ceiling", "facing=south", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6417, "face=ceiling", "facing=south", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6418, "face=ceiling", "facing=west", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6419, "face=ceiling", "facing=west", "powered=false"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6420, "face=ceiling", "facing=east", "powered=true"));
        Block.BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 6421, "face=ceiling", "facing=east", "powered=false"));
    }
}
