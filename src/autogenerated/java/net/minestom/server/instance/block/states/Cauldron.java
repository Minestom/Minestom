package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Cauldron {
	public static void initStates() {
		CAULDRON.addBlockAlternative(new BlockAlternative((short) 5141, "level=0"));
		CAULDRON.addBlockAlternative(new BlockAlternative((short) 5142, "level=1"));
		CAULDRON.addBlockAlternative(new BlockAlternative((short) 5143, "level=2"));
		CAULDRON.addBlockAlternative(new BlockAlternative((short) 5144, "level=3"));
	}
}
