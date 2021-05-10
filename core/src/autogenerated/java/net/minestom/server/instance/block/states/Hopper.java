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
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6732, "enabled=true", "facing=down"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6733, "enabled=true", "facing=north"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6734, "enabled=true", "facing=south"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6735, "enabled=true", "facing=west"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6736, "enabled=true", "facing=east"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6737, "enabled=false", "facing=down"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6738, "enabled=false", "facing=north"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6739, "enabled=false", "facing=south"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6740, "enabled=false", "facing=west"));
        Block.HOPPER.addBlockAlternative(new BlockAlternative((short) 6741, "enabled=false", "facing=east"));
    }
}
