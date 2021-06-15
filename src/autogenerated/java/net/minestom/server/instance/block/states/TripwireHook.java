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
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5465, "attached=true", "facing=north", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5466, "attached=true", "facing=north", "powered=false"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5467, "attached=true", "facing=south", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5468, "attached=true", "facing=south", "powered=false"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5469, "attached=true", "facing=west", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5470, "attached=true", "facing=west", "powered=false"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5471, "attached=true", "facing=east", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5472, "attached=true", "facing=east", "powered=false"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5473, "attached=false", "facing=north", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5474, "attached=false", "facing=north", "powered=false"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5475, "attached=false", "facing=south", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5476, "attached=false", "facing=south", "powered=false"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5477, "attached=false", "facing=west", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5478, "attached=false", "facing=west", "powered=false"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5479, "attached=false", "facing=east", "powered=true"));
        Block.TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5480, "attached=false", "facing=east", "powered=false"));
    }
}
