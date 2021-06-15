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
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15757, "face=floor", "facing=north", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15758, "face=floor", "facing=north", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15759, "face=floor", "facing=south", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15760, "face=floor", "facing=south", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15761, "face=floor", "facing=west", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15762, "face=floor", "facing=west", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15763, "face=floor", "facing=east", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15764, "face=floor", "facing=east", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15765, "face=wall", "facing=north", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15766, "face=wall", "facing=north", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15767, "face=wall", "facing=south", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15768, "face=wall", "facing=south", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15769, "face=wall", "facing=west", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15770, "face=wall", "facing=west", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15771, "face=wall", "facing=east", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15772, "face=wall", "facing=east", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15773, "face=ceiling", "facing=north", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15774, "face=ceiling", "facing=north", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15775, "face=ceiling", "facing=south", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15776, "face=ceiling", "facing=south", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15777, "face=ceiling", "facing=west", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15778, "face=ceiling", "facing=west", "powered=false"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15779, "face=ceiling", "facing=east", "powered=true"));
        Block.WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15780, "face=ceiling", "facing=east", "powered=false"));
    }
}
