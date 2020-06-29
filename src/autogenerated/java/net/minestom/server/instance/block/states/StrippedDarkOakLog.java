package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedDarkOakLog {
	public static void initStates() {
		STRIPPED_DARK_OAK_LOG.addBlockAlternative(new BlockAlternative((short) 103, "axis=x"));
		STRIPPED_DARK_OAK_LOG.addBlockAlternative(new BlockAlternative((short) 104, "axis=y"));
		STRIPPED_DARK_OAK_LOG.addBlockAlternative(new BlockAlternative((short) 105, "axis=z"));
	}
}
