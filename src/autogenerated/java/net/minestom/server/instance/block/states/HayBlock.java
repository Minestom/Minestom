package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class HayBlock {
	public static void initStates() {
		HAY_BLOCK.addBlockAlternative(new BlockAlternative((short) 7863, "axis=x"));
		HAY_BLOCK.addBlockAlternative(new BlockAlternative((short) 7864, "axis=y"));
		HAY_BLOCK.addBlockAlternative(new BlockAlternative((short) 7865, "axis=z"));
	}
}
