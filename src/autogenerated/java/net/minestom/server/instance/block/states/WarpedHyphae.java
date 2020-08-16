package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class WarpedHyphae {
	public static void initStates() {
		WARPED_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14972, "axis=x"));
		WARPED_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14973, "axis=y"));
		WARPED_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14974, "axis=z"));
	}
}
