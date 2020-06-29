package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SpruceLog {
	public static void initStates() {
		SPRUCE_LOG.addBlockAlternative(new BlockAlternative((short) 76, "axis=x"));
		SPRUCE_LOG.addBlockAlternative(new BlockAlternative((short) 77, "axis=y"));
		SPRUCE_LOG.addBlockAlternative(new BlockAlternative((short) 78, "axis=z"));
	}
}
