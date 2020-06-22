package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OakLog {
	public static void initStates() {
		OAK_LOG.addBlockAlternative(new BlockAlternative((short) 72, "axis=x"));
		OAK_LOG.addBlockAlternative(new BlockAlternative((short) 73, "axis=y"));
		OAK_LOG.addBlockAlternative(new BlockAlternative((short) 74, "axis=z"));
	}
}
