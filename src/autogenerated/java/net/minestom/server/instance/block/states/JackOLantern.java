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
public final class JackOLantern {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.JACK_O_LANTERN.addBlockAlternative(new BlockAlternative((short) 4089, "facing=north"));
        Block.JACK_O_LANTERN.addBlockAlternative(new BlockAlternative((short) 4090, "facing=south"));
        Block.JACK_O_LANTERN.addBlockAlternative(new BlockAlternative((short) 4091, "facing=west"));
        Block.JACK_O_LANTERN.addBlockAlternative(new BlockAlternative((short) 4092, "facing=east"));
    }
}
