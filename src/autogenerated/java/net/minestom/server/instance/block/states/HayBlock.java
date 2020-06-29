package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class HayBlock {
	public static void initStates() {
		HAY_BLOCK.addBlockAlternative(new BlockAlternative((short) 7327, "axis=x"));
		HAY_BLOCK.addBlockAlternative(new BlockAlternative((short) 7328, "axis=y"));
		HAY_BLOCK.addBlockAlternative(new BlockAlternative((short) 7329, "axis=z"));
	}
}
