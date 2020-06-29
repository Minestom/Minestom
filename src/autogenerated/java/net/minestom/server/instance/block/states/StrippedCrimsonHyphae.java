package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedCrimsonHyphae {
	public static void initStates() {
		STRIPPED_CRIMSON_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14984, "axis=x"));
		STRIPPED_CRIMSON_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14985, "axis=y"));
		STRIPPED_CRIMSON_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14986, "axis=z"));
	}
}
