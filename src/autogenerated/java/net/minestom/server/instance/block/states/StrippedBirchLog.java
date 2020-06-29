package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedBirchLog {
	public static void initStates() {
		STRIPPED_BIRCH_LOG.addBlockAlternative(new BlockAlternative((short) 94, "axis=x"));
		STRIPPED_BIRCH_LOG.addBlockAlternative(new BlockAlternative((short) 95, "axis=y"));
		STRIPPED_BIRCH_LOG.addBlockAlternative(new BlockAlternative((short) 96, "axis=z"));
	}
}
