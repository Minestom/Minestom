package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedWarpedHyphae {
	public static void initStates() {
		STRIPPED_WARPED_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14975, "axis=x"));
		STRIPPED_WARPED_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14976, "axis=y"));
		STRIPPED_WARPED_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14977, "axis=z"));
	}
}
