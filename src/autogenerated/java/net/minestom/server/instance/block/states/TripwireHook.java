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
public final class TripwireHook {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5263, "attached=true", "facing=north", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5264, "attached=true", "facing=north", "powered=false"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5265, "attached=true", "facing=south", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5266, "attached=true", "facing=south", "powered=false"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5267, "attached=true", "facing=west", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5268, "attached=true", "facing=west", "powered=false"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5269, "attached=true", "facing=east", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5270, "attached=true", "facing=east", "powered=false"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5271, "attached=false", "facing=north", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5272, "attached=false", "facing=north", "powered=false"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5273, "attached=false", "facing=south", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5274, "attached=false", "facing=south", "powered=false"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5275, "attached=false", "facing=west", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5276, "attached=false", "facing=west", "powered=false"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5277, "attached=false", "facing=east", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5278, "attached=false", "facing=east", "powered=false"));
    }
}
