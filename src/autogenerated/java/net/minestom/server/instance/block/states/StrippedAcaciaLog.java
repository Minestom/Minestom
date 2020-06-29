package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedAcaciaLog {
	public static void initStates() {
		STRIPPED_ACACIA_LOG.addBlockAlternative(new BlockAlternative((short) 100, "axis=x"));
		STRIPPED_ACACIA_LOG.addBlockAlternative(new BlockAlternative((short) 101, "axis=y"));
		STRIPPED_ACACIA_LOG.addBlockAlternative(new BlockAlternative((short) 102, "axis=z"));
	}
}
