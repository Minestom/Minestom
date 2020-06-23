package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BrownShulkerBox {
	public static void initStates() {
		BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8814, "facing=north"));
		BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8815, "facing=east"));
		BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8816, "facing=south"));
		BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8817, "facing=west"));
		BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8818, "facing=up"));
		BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8819, "facing=down"));
	}
}
