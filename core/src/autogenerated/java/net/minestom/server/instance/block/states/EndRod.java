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
public final class EndRod {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.END_ROD.addBlockAlternative(new BlockAlternative((short) 9062, "facing=north"));
        Block.END_ROD.addBlockAlternative(new BlockAlternative((short) 9063, "facing=east"));
        Block.END_ROD.addBlockAlternative(new BlockAlternative((short) 9064, "facing=south"));
        Block.END_ROD.addBlockAlternative(new BlockAlternative((short) 9065, "facing=west"));
        Block.END_ROD.addBlockAlternative(new BlockAlternative((short) 9066, "facing=up"));
        Block.END_ROD.addBlockAlternative(new BlockAlternative((short) 9067, "facing=down"));
    }
}
