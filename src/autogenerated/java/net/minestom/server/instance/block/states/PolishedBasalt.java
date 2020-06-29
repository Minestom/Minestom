package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PolishedBasalt {
	public static void initStates() {
		POLISHED_BASALT.addBlockAlternative(new BlockAlternative((short) 4005, "axis=x"));
		POLISHED_BASALT.addBlockAlternative(new BlockAlternative((short) 4006, "axis=y"));
		POLISHED_BASALT.addBlockAlternative(new BlockAlternative((short) 4007, "axis=z"));
	}
}
