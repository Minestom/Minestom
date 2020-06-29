package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BrownShulkerBox {
	public static void initStates() {
		BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9350, "facing=north"));
		BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9351, "facing=east"));
		BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9352, "facing=south"));
		BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9353, "facing=west"));
		BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9354, "facing=up"));
		BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9355, "facing=down"));
	}
}
