package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GrayShulkerBox {
	public static void initStates() {
		GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8784, "facing=north"));
		GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8785, "facing=east"));
		GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8786, "facing=south"));
		GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8787, "facing=west"));
		GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8788, "facing=up"));
		GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8789, "facing=down"));
	}
}
