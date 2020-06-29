package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Basalt {
	public static void initStates() {
		BASALT.addBlockAlternative(new BlockAlternative((short) 4002, "axis=x"));
		BASALT.addBlockAlternative(new BlockAlternative((short) 4003, "axis=y"));
		BASALT.addBlockAlternative(new BlockAlternative((short) 4004, "axis=z"));
	}
}
