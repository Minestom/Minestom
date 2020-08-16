package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Dropper {
	public static void initStates() {
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6839, "facing=north", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6840, "facing=north", "triggered=false"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6841, "facing=east", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6842, "facing=east", "triggered=false"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6843, "facing=south", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6844, "facing=south", "triggered=false"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6845, "facing=west", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6846, "facing=west", "triggered=false"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6847, "facing=up", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6848, "facing=up", "triggered=false"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6849, "facing=down", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6850, "facing=down", "triggered=false"));
	}
}
