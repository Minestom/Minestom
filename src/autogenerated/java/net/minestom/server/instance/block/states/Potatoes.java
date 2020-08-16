package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Potatoes {
	public static void initStates() {
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6342, "age=0"));
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6343, "age=1"));
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6344, "age=2"));
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6345, "age=3"));
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6346, "age=4"));
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6347, "age=5"));
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6348, "age=6"));
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6349, "age=7"));
	}
}
