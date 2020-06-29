package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class NetherPortal {
	public static void initStates() {
		NETHER_PORTAL.addBlockAlternative(new BlockAlternative((short) 4014, "axis=x"));
		NETHER_PORTAL.addBlockAlternative(new BlockAlternative((short) 4015, "axis=z"));
	}
}
