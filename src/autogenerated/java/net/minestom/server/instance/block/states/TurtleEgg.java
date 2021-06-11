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
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9748, "eggs=1", "hatch=0"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9749, "eggs=1", "hatch=1"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9750, "eggs=1", "hatch=2"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9751, "eggs=2", "hatch=0"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9752, "eggs=2", "hatch=1"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9753, "eggs=2", "hatch=2"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9754, "eggs=3", "hatch=0"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9755, "eggs=3", "hatch=1"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9756, "eggs=3", "hatch=2"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9757, "eggs=4", "hatch=0"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9758, "eggs=4", "hatch=1"));
        Block.TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 9759, "eggs=4", "hatch=2"));
    }
}
