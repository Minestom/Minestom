package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlueShulkerBox {
	public static void initStates() {
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9344, "facing=north"));
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9345, "facing=east"));
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9346, "facing=south"));
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9347, "facing=west"));
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9348, "facing=up"));
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9349, "facing=down"));
	}
}
