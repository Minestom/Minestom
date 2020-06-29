package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedJungleLog {
	public static void initStates() {
		STRIPPED_JUNGLE_LOG.addBlockAlternative(new BlockAlternative((short) 97, "axis=x"));
		STRIPPED_JUNGLE_LOG.addBlockAlternative(new BlockAlternative((short) 98, "axis=y"));
		STRIPPED_JUNGLE_LOG.addBlockAlternative(new BlockAlternative((short) 99, "axis=z"));
	}
}
