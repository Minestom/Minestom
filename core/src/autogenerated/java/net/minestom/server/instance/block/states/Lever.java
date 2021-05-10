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
public final class Lever {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3783, "face=floor", "facing=north", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3784, "face=floor", "facing=north", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3785, "face=floor", "facing=south", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3786, "face=floor", "facing=south", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3787, "face=floor", "facing=west", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3788, "face=floor", "facing=west", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3789, "face=floor", "facing=east", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3790, "face=floor", "facing=east", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3791, "face=wall", "facing=north", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3792, "face=wall", "facing=north", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3793, "face=wall", "facing=south", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3794, "face=wall", "facing=south", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3795, "face=wall", "facing=west", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3796, "face=wall", "facing=west", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3797, "face=wall", "facing=east", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3798, "face=wall", "facing=east", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3799, "face=ceiling", "facing=north", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3800, "face=ceiling", "facing=north", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3801, "face=ceiling", "facing=south", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3802, "face=ceiling", "facing=south", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3803, "face=ceiling", "facing=west", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3804, "face=ceiling", "facing=west", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3805, "face=ceiling", "facing=east", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3806, "face=ceiling", "facing=east", "powered=false"));
    }
}
