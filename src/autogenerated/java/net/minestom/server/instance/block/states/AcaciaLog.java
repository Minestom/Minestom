package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class AcaciaLog {
	public static void initStates() {
		ACACIA_LOG.addBlockAlternative(new BlockAlternative((short) 85, "axis=x"));
		ACACIA_LOG.addBlockAlternative(new BlockAlternative((short) 86, "axis=y"));
		ACACIA_LOG.addBlockAlternative(new BlockAlternative((short) 87, "axis=z"));
	}
}
