package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedOakLog {
	public static void initStates() {
		STRIPPED_OAK_LOG.addBlockAlternative(new BlockAlternative((short) 106, "axis=x"));
		STRIPPED_OAK_LOG.addBlockAlternative(new BlockAlternative((short) 107, "axis=y"));
		STRIPPED_OAK_LOG.addBlockAlternative(new BlockAlternative((short) 108, "axis=z"));
	}
}
