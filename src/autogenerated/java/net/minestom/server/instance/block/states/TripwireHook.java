package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class TripwireHook {
	public static void initStates() {
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5243, "attached=true", "facing=north", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5244, "attached=true", "facing=north", "powered=false"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5245, "attached=true", "facing=south", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5246, "attached=true", "facing=south", "powered=false"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5247, "attached=true", "facing=west", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5248, "attached=true", "facing=west", "powered=false"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5249, "attached=true", "facing=east", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5250, "attached=true", "facing=east", "powered=false"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5251, "attached=false", "facing=north", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5252, "attached=false", "facing=north", "powered=false"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5253, "attached=false", "facing=south", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5254, "attached=false", "facing=south", "powered=false"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5255, "attached=false", "facing=west", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5256, "attached=false", "facing=west", "powered=false"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5257, "attached=false", "facing=east", "powered=true"));
		TRIPWIRE_HOOK.addBlockAlternative(new BlockAlternative((short) 5258, "attached=false", "facing=east", "powered=false"));
	}
}
