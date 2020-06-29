package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GrayShulkerBox {
	public static void initStates() {
		GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9320, "facing=north"));
		GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9321, "facing=east"));
		GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9322, "facing=south"));
		GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9323, "facing=west"));
		GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9324, "facing=up"));
		GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9325, "facing=down"));
	}
}
