package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class TurtleEgg {
	public static void initStates() {
		TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 8962, "eggs=1", "hatch=0"));
		TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 8963, "eggs=1", "hatch=1"));
		TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 8964, "eggs=1", "hatch=2"));
		TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 8965, "eggs=2", "hatch=0"));
		TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 8966, "eggs=2", "hatch=1"));
		TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 8967, "eggs=2", "hatch=2"));
		TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 8968, "eggs=3", "hatch=0"));
		TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 8969, "eggs=3", "hatch=1"));
		TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 8970, "eggs=3", "hatch=2"));
		TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 8971, "eggs=4", "hatch=0"));
		TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 8972, "eggs=4", "hatch=1"));
		TURTLE_EGG.addBlockAlternative(new BlockAlternative((short) 8973, "eggs=4", "hatch=2"));
	}
}
