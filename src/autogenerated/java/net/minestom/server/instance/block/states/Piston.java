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
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1404, "extended=true", "facing=north"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1405, "extended=true", "facing=east"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1406, "extended=true", "facing=south"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1407, "extended=true", "facing=west"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1408, "extended=true", "facing=up"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1409, "extended=true", "facing=down"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1410, "extended=false", "facing=north"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1411, "extended=false", "facing=east"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1412, "extended=false", "facing=south"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1413, "extended=false", "facing=west"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1414, "extended=false", "facing=up"));
        Block.PISTON.addBlockAlternative(new BlockAlternative((short) 1415, "extended=false", "facing=down"));
    }
}
