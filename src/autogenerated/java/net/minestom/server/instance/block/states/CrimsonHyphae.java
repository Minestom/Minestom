package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CrimsonHyphae {
	public static void initStates() {
		CRIMSON_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14989, "axis=x"));
		CRIMSON_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14990, "axis=y"));
		CRIMSON_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14991, "axis=z"));
	}
}
