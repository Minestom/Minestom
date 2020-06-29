package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CrimsonStem {
	public static void initStates() {
		CRIMSON_STEM.addBlockAlternative(new BlockAlternative((short) 14975, "axis=x"));
		CRIMSON_STEM.addBlockAlternative(new BlockAlternative((short) 14976, "axis=y"));
		CRIMSON_STEM.addBlockAlternative(new BlockAlternative((short) 14977, "axis=z"));
	}
}
