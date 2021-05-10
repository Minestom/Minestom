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
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9264, "facing=north", "powered=true"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9265, "facing=north", "powered=false"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9266, "facing=east", "powered=true"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9267, "facing=east", "powered=false"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9268, "facing=south", "powered=true"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9269, "facing=south", "powered=false"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9270, "facing=west", "powered=true"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9271, "facing=west", "powered=false"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9272, "facing=up", "powered=true"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9273, "facing=up", "powered=false"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9274, "facing=down", "powered=true"));
        Block.OBSERVER.addBlockAlternative(new BlockAlternative((short) 9275, "facing=down", "powered=false"));
    }
}
