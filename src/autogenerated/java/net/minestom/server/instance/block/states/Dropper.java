package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Dropper {
	public static void initStates() {
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6835, "facing=north", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6836, "facing=north", "triggered=false"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6837, "facing=east", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6838, "facing=east", "triggered=false"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6839, "facing=south", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6840, "facing=south", "triggered=false"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6841, "facing=west", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6842, "facing=west", "triggered=false"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6843, "facing=up", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6844, "facing=up", "triggered=false"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6845, "facing=down", "triggered=true"));
		DROPPER.addBlockAlternative(new BlockAlternative((short) 6846, "facing=down", "triggered=false"));
	}
}
