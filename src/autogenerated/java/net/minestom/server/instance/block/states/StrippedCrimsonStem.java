package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedCrimsonStem {
	public static void initStates() {
		STRIPPED_CRIMSON_STEM.addBlockAlternative(new BlockAlternative((short) 14986, "axis=x"));
		STRIPPED_CRIMSON_STEM.addBlockAlternative(new BlockAlternative((short) 14987, "axis=y"));
		STRIPPED_CRIMSON_STEM.addBlockAlternative(new BlockAlternative((short) 14988, "axis=z"));
	}
}
