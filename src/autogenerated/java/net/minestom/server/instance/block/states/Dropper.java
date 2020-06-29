package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Dropper {
	public static void initStates() {
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6299, "facing=north", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6300, "facing=north", "triggered=false"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6301, "facing=east", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6302, "facing=east", "triggered=false"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6303, "facing=south", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6304, "facing=south", "triggered=false"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6305, "facing=west", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6306, "facing=west", "triggered=false"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6307, "facing=up", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6308, "facing=up", "triggered=false"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6309, "facing=down", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6310, "facing=down", "triggered=false"));
	}
}
