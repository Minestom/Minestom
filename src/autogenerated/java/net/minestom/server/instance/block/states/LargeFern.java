package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class LargeFern {
	public static void initStates() {
		LARGE_FERN.addBlockAlternative(new BlockAlternative((short) 7899, "half=upper"));
		LARGE_FERN.addBlockAlternative(new BlockAlternative((short) 7900, "half=lower"));
	}
}
