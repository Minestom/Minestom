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
public final class EndPortalFrame {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5150, "eye=true", "facing=north"));
        Block.END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5151, "eye=true", "facing=south"));
        Block.END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5152, "eye=true", "facing=west"));
        Block.END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5153, "eye=true", "facing=east"));
        Block.END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5154, "eye=false", "facing=north"));
        Block.END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5155, "eye=false", "facing=south"));
        Block.END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5156, "eye=false", "facing=west"));
        Block.END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5157, "eye=false", "facing=east"));
    }
}
