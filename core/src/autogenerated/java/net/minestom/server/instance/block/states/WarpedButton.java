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
public final class WarpedButton {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15511, "face=floor", "facing=north", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15512, "face=floor", "facing=north", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15513, "face=floor", "facing=south", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15514, "face=floor", "facing=south", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15515, "face=floor", "facing=west", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15516, "face=floor", "facing=west", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15517, "face=floor", "facing=east", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15518, "face=floor", "facing=east", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15519, "face=wall", "facing=north", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15520, "face=wall", "facing=north", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15521, "face=wall", "facing=south", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15522, "face=wall", "facing=south", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15523, "face=wall", "facing=west", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15524, "face=wall", "facing=west", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15525, "face=wall", "facing=east", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15526, "face=wall", "facing=east", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15527, "face=ceiling", "facing=north", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15528, "face=ceiling", "facing=north", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15529, "face=ceiling", "facing=south", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15530, "face=ceiling", "facing=south", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15531, "face=ceiling", "facing=west", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15532, "face=ceiling", "facing=west", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15533, "face=ceiling", "facing=east", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15534, "face=ceiling", "facing=east", "powered=false"));
    }
}
