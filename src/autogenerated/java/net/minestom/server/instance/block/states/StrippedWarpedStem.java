package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedWarpedStem {
	public static void initStates() {
		STRIPPED_WARPED_STEM.addBlockAlternative(new BlockAlternative((short) 14969, "axis=x"));
		STRIPPED_WARPED_STEM.addBlockAlternative(new BlockAlternative((short) 14970, "axis=y"));
		STRIPPED_WARPED_STEM.addBlockAlternative(new BlockAlternative((short) 14971, "axis=z"));
	}
}
