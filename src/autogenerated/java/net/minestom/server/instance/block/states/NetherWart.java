package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class NetherWart {
	public static void initStates() {
		NETHER_WART.addBlockAlternative(new BlockAlternative((short) 5128, "age=0"));
		NETHER_WART.addBlockAlternative(new BlockAlternative((short) 5129, "age=1"));
		NETHER_WART.addBlockAlternative(new BlockAlternative((short) 5130, "age=2"));
		NETHER_WART.addBlockAlternative(new BlockAlternative((short) 5131, "age=3"));
	}
}
