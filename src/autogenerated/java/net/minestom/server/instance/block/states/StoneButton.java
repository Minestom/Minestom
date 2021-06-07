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
public final class StoneButton {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3966, "face=floor", "facing=north", "powered=true"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3967, "face=floor", "facing=north", "powered=false"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3968, "face=floor", "facing=south", "powered=true"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3969, "face=floor", "facing=south", "powered=false"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3970, "face=floor", "facing=west", "powered=true"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3971, "face=floor", "facing=west", "powered=false"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3972, "face=floor", "facing=east", "powered=true"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3973, "face=floor", "facing=east", "powered=false"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3974, "face=wall", "facing=north", "powered=true"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3975, "face=wall", "facing=north", "powered=false"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3976, "face=wall", "facing=south", "powered=true"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3977, "face=wall", "facing=south", "powered=false"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3978, "face=wall", "facing=west", "powered=true"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3979, "face=wall", "facing=west", "powered=false"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3980, "face=wall", "facing=east", "powered=true"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3981, "face=wall", "facing=east", "powered=false"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3982, "face=ceiling", "facing=north", "powered=true"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3983, "face=ceiling", "facing=north", "powered=false"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3984, "face=ceiling", "facing=south", "powered=true"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3985, "face=ceiling", "facing=south", "powered=false"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3986, "face=ceiling", "facing=west", "powered=true"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3987, "face=ceiling", "facing=west", "powered=false"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3988, "face=ceiling", "facing=east", "powered=true"));
        Block.STONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 3989, "face=ceiling", "facing=east", "powered=false"));
    }
}
