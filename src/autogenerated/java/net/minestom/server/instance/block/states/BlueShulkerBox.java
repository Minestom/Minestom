package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlueShulkerBox {
	public static void initStates() {
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9348, "facing=north"));
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9349, "facing=east"));
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9350, "facing=south"));
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9351, "facing=west"));
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9352, "facing=up"));
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9353, "facing=down"));
	}
}
