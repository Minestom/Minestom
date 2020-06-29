package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Dispenser {
	public static void initStates() {
		DISPENSER.addBlockAlternative(new BlockAlternative((short) 234, "facing=north", "triggered=true"));
		DISPENSER.addBlockAlternative(new BlockAlternative((short) 235, "facing=north", "triggered=false"));
		DISPENSER.addBlockAlternative(new BlockAlternative((short) 236, "facing=east", "triggered=true"));
		DISPENSER.addBlockAlternative(new BlockAlternative((short) 237, "facing=east", "triggered=false"));
		DISPENSER.addBlockAlternative(new BlockAlternative((short) 238, "facing=south", "triggered=true"));
		DISPENSER.addBlockAlternative(new BlockAlternative((short) 239, "facing=south", "triggered=false"));
		DISPENSER.addBlockAlternative(new BlockAlternative((short) 240, "facing=west", "triggered=true"));
		DISPENSER.addBlockAlternative(new BlockAlternative((short) 241, "facing=west", "triggered=false"));
		DISPENSER.addBlockAlternative(new BlockAlternative((short) 242, "facing=up", "triggered=true"));
		DISPENSER.addBlockAlternative(new BlockAlternative((short) 243, "facing=up", "triggered=false"));
		DISPENSER.addBlockAlternative(new BlockAlternative((short) 244, "facing=down", "triggered=true"));
		DISPENSER.addBlockAlternative(new BlockAlternative((short) 245, "facing=down", "triggered=false"));
	}
}
