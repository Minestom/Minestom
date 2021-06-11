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
public final class Hopper {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6934, "enabled=true", "facing=down"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6935, "enabled=true", "facing=north"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6936, "enabled=true", "facing=south"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6937, "enabled=true", "facing=west"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6938, "enabled=true", "facing=east"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6939, "enabled=false", "facing=down"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6940, "enabled=false", "facing=north"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6941, "enabled=false", "facing=south"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6942, "enabled=false", "facing=west"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6943, "enabled=false", "facing=east"));
    }
}
