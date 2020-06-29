package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SweetBerryBush {
	public static void initStates() {
		SWEET_BERRY_BUSH.addBlockAlternative(new BlockAlternative((short) 14954, "age=0"));
		SWEET_BERRY_BUSH.addBlockAlternative(new BlockAlternative((short) 14955, "age=1"));
		SWEET_BERRY_BUSH.addBlockAlternative(new BlockAlternative((short) 14956, "age=2"));
		SWEET_BERRY_BUSH.addBlockAlternative(new BlockAlternative((short) 14957, "age=3"));
	}
}
