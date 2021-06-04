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
public final class Cocoa {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.COCOA.addBlockAlternative(new BlockAlternative((short) 5162, "age=0", "facing=north"));
        Block.COCOA.addBlockAlternative(new BlockAlternative((short) 5163, "age=0", "facing=south"));
        Block.COCOA.addBlockAlternative(new BlockAlternative((short) 5164, "age=0", "facing=west"));
        Block.COCOA.addBlockAlternative(new BlockAlternative((short) 5165, "age=0", "facing=east"));
        Block.COCOA.addBlockAlternative(new BlockAlternative((short) 5166, "age=1", "facing=north"));
        Block.COCOA.addBlockAlternative(new BlockAlternative((short) 5167, "age=1", "facing=south"));
        Block.COCOA.addBlockAlternative(new BlockAlternative((short) 5168, "age=1", "facing=west"));
        Block.COCOA.addBlockAlternative(new BlockAlternative((short) 5169, "age=1", "facing=east"));
        Block.COCOA.addBlockAlternative(new BlockAlternative((short) 5170, "age=2", "facing=north"));
        Block.COCOA.addBlockAlternative(new BlockAlternative((short) 5171, "age=2", "facing=south"));
        Block.COCOA.addBlockAlternative(new BlockAlternative((short) 5172, "age=2", "facing=west"));
        Block.COCOA.addBlockAlternative(new BlockAlternative((short) 5173, "age=2", "facing=east"));
    }
}
