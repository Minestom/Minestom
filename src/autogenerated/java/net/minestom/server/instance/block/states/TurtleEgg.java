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
public final class TurtleEgg {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9502, "eggs=1", "hatch=0"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9503, "eggs=1", "hatch=1"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9504, "eggs=1", "hatch=2"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9505, "eggs=2", "hatch=0"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9506, "eggs=2", "hatch=1"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9507, "eggs=2", "hatch=2"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9508, "eggs=3", "hatch=0"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9509, "eggs=3", "hatch=1"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9510, "eggs=3", "hatch=2"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9511, "eggs=4", "hatch=0"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9512, "eggs=4", "hatch=1"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9513, "eggs=4", "hatch=2"));
    }
}
