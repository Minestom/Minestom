package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class TripwireHook {
	public static void initStates() {
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5263, "attached=true", "facing=north", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5264, "attached=true", "facing=north", "powered=false"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5265, "attached=true", "facing=south", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5266, "attached=true", "facing=south", "powered=false"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5267, "attached=true", "facing=west", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5268, "attached=true", "facing=west", "powered=false"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5269, "attached=true", "facing=east", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5270, "attached=true", "facing=east", "powered=false"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5271, "attached=false", "facing=north", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5272, "attached=false", "facing=north", "powered=false"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5273, "attached=false", "facing=south", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5274, "attached=false", "facing=south", "powered=false"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5275, "attached=false", "facing=west", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5276, "attached=false", "facing=west", "powered=false"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5277, "attached=false", "facing=east", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5278, "attached=false", "facing=east", "powered=false"));
	}
}
