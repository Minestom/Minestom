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
public final class Piston {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1348, "extended=true", "facing=north"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1349, "extended=true", "facing=east"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1350, "extended=true", "facing=south"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1351, "extended=true", "facing=west"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1352, "extended=true", "facing=up"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1353, "extended=true", "facing=down"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1354, "extended=false", "facing=north"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1355, "extended=false", "facing=east"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1356, "extended=false", "facing=south"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1357, "extended=false", "facing=west"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1358, "extended=false", "facing=up"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1359, "extended=false", "facing=down"));
    }
}
