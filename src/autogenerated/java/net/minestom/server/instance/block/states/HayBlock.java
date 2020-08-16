package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class HayBlock {
	public static void initStates() {
		HAY_BLOCK.addBlockAlternative(new BlockAlternative((short) 7867, "axis=x"));
		HAY_BLOCK.addBlockAlternative(new BlockAlternative((short) 7868, "axis=y"));
		HAY_BLOCK.addBlockAlternative(new BlockAlternative((short) 7869, "axis=z"));
	}
}
