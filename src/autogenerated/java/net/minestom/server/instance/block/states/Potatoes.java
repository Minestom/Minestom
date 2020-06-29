package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Potatoes {
	public static void initStates() {
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6338, "age=0"));
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6339, "age=1"));
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6340, "age=2"));
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6341, "age=3"));
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6342, "age=4"));
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6343, "age=5"));
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6344, "age=6"));
		POTATOES.addBlockAlternative(new BlockAlternative((short) 6345, "age=7"));
	}
}
