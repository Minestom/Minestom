package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Lava {
	public static void initStates() {
		LAVA.addBlockAlternative(new BlockAlternative((short) 50, "level=0"));
		LAVA.addBlockAlternative(new BlockAlternative((short) 51, "level=1"));
		LAVA.addBlockAlternative(new BlockAlternative((short) 52, "level=2"));
		LAVA.addBlockAlternative(new BlockAlternative((short) 53, "level=3"));
		LAVA.addBlockAlternative(new BlockAlternative((short) 54, "level=4"));
		LAVA.addBlockAlternative(new BlockAlternative((short) 55, "level=5"));
		LAVA.addBlockAlternative(new BlockAlternative((short) 56, "level=6"));
		LAVA.addBlockAlternative(new BlockAlternative((short) 57, "level=7"));
		LAVA.addBlockAlternative(new BlockAlternative((short) 58, "level=8"));
		LAVA.addBlockAlternative(new BlockAlternative((short) 59, "level=9"));
		LAVA.addBlockAlternative(new BlockAlternative((short) 60, "level=10"));
		LAVA.addBlockAlternative(new BlockAlternative((short) 61, "level=11"));
		LAVA.addBlockAlternative(new BlockAlternative((short) 62, "level=12"));
		LAVA.addBlockAlternative(new BlockAlternative((short) 63, "level=13"));
		LAVA.addBlockAlternative(new BlockAlternative((short) 64, "level=14"));
		LAVA.addBlockAlternative(new BlockAlternative((short) 65, "level=15"));
	}
}
