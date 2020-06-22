package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BirchLog {
	public static void initStates() {
		BIRCH_LOG.addBlockAlternative(new BlockAlternative((short) 78, "axis=x"));
		BIRCH_LOG.addBlockAlternative(new BlockAlternative((short) 79, "axis=y"));
		BIRCH_LOG.addBlockAlternative(new BlockAlternative((short) 80, "axis=z"));
	}
}
