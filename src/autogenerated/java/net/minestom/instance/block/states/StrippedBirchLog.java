package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedBirchLog {
	public static void initStates() {
		STRIPPED_BIRCH_LOG.addBlockAlternative(new BlockAlternative((short) 93, "axis=x"));
		STRIPPED_BIRCH_LOG.addBlockAlternative(new BlockAlternative((short) 94, "axis=y"));
		STRIPPED_BIRCH_LOG.addBlockAlternative(new BlockAlternative((short) 95, "axis=z"));
	}
}
