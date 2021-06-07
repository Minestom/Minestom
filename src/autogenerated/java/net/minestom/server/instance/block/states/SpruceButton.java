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
public final class SpruceButton {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6576, "face=floor", "facing=north", "powered=true"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6577, "face=floor", "facing=north", "powered=false"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6578, "face=floor", "facing=south", "powered=true"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6579, "face=floor", "facing=south", "powered=false"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6580, "face=floor", "facing=west", "powered=true"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6581, "face=floor", "facing=west", "powered=false"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6582, "face=floor", "facing=east", "powered=true"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6583, "face=floor", "facing=east", "powered=false"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6584, "face=wall", "facing=north", "powered=true"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6585, "face=wall", "facing=north", "powered=false"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6586, "face=wall", "facing=south", "powered=true"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6587, "face=wall", "facing=south", "powered=false"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6588, "face=wall", "facing=west", "powered=true"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6589, "face=wall", "facing=west", "powered=false"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6590, "face=wall", "facing=east", "powered=true"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6591, "face=wall", "facing=east", "powered=false"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6592, "face=ceiling", "facing=north", "powered=true"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6593, "face=ceiling", "facing=north", "powered=false"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6594, "face=ceiling", "facing=south", "powered=true"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6595, "face=ceiling", "facing=south", "powered=false"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6596, "face=ceiling", "facing=west", "powered=true"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6597, "face=ceiling", "facing=west", "powered=false"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6598, "face=ceiling", "facing=east", "powered=true"));
        Block.SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6599, "face=ceiling", "facing=east", "powered=false"));
    }
}
