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
public final class OakButton {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6552, "face=floor", "facing=north", "powered=true"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6553, "face=floor", "facing=north", "powered=false"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6554, "face=floor", "facing=south", "powered=true"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6555, "face=floor", "facing=south", "powered=false"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6556, "face=floor", "facing=west", "powered=true"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6557, "face=floor", "facing=west", "powered=false"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6558, "face=floor", "facing=east", "powered=true"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6559, "face=floor", "facing=east", "powered=false"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6560, "face=wall", "facing=north", "powered=true"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6561, "face=wall", "facing=north", "powered=false"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6562, "face=wall", "facing=south", "powered=true"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6563, "face=wall", "facing=south", "powered=false"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6564, "face=wall", "facing=west", "powered=true"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6565, "face=wall", "facing=west", "powered=false"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6566, "face=wall", "facing=east", "powered=true"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6567, "face=wall", "facing=east", "powered=false"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6568, "face=ceiling", "facing=north", "powered=true"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6569, "face=ceiling", "facing=north", "powered=false"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6570, "face=ceiling", "facing=south", "powered=true"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6571, "face=ceiling", "facing=south", "powered=false"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6572, "face=ceiling", "facing=west", "powered=true"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6573, "face=ceiling", "facing=west", "powered=false"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6574, "face=ceiling", "facing=east", "powered=true"));
        Block.OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6575, "face=ceiling", "facing=east", "powered=false"));
    }
}
