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
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3850, "face=floor", "facing=north", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3851, "face=floor", "facing=north", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3852, "face=floor", "facing=south", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3853, "face=floor", "facing=south", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3854, "face=floor", "facing=west", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3855, "face=floor", "facing=west", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3856, "face=floor", "facing=east", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3857, "face=floor", "facing=east", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3858, "face=wall", "facing=north", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3859, "face=wall", "facing=north", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3860, "face=wall", "facing=south", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3861, "face=wall", "facing=south", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3862, "face=wall", "facing=west", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3863, "face=wall", "facing=west", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3864, "face=wall", "facing=east", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3865, "face=wall", "facing=east", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3866, "face=ceiling", "facing=north", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3867, "face=ceiling", "facing=north", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3868, "face=ceiling", "facing=south", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3869, "face=ceiling", "facing=south", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3870, "face=ceiling", "facing=west", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3871, "face=ceiling", "facing=west", "powered=false"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3872, "face=ceiling", "facing=east", "powered=true"));
        Block.LEVER.addBlockAlternative(new BlockAlternative((short) 3873, "face=ceiling", "facing=east", "powered=false"));
    }
}
