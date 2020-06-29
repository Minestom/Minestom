package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedSpruceLog {
	public static void initStates() {
		STRIPPED_SPRUCE_LOG.addBlockAlternative(new BlockAlternative((short) 91, "axis=x"));
		STRIPPED_SPRUCE_LOG.addBlockAlternative(new BlockAlternative((short) 92, "axis=y"));
		STRIPPED_SPRUCE_LOG.addBlockAlternative(new BlockAlternative((short) 93, "axis=z"));
	}
}
