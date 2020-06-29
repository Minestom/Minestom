package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class TallSeagrass {
	public static void initStates() {
		TALL_SEAGRASS.addBlockAlternative(new BlockAlternative((short) 1346, "half=upper"));
		TALL_SEAGRASS.addBlockAlternative(new BlockAlternative((short) 1347, "half=lower"));
	}
}
