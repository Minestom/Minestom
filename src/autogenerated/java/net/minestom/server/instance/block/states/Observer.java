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
public final class Observer {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9510, "facing=north", "powered=true"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9511, "facing=north", "powered=false"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9512, "facing=east", "powered=true"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9513, "facing=east", "powered=false"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9514, "facing=south", "powered=true"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9515, "facing=south", "powered=false"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9516, "facing=west", "powered=true"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9517, "facing=west", "powered=false"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9518, "facing=up", "powered=true"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9519, "facing=up", "powered=false"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9520, "facing=down", "powered=true"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9521, "facing=down", "powered=false"));
    }
}
