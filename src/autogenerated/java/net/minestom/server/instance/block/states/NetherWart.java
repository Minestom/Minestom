package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class NetherWart {
	public static void initStates() {
		NETHER_WART.addBlockAlternative(new BlockAlternative((short) 5112, "age=0"));
		NETHER_WART.addBlockAlternative(new BlockAlternative((short) 5113, "age=1"));
		NETHER_WART.addBlockAlternative(new BlockAlternative((short) 5114, "age=2"));
		NETHER_WART.addBlockAlternative(new BlockAlternative((short) 5115, "age=3"));
	}
}
