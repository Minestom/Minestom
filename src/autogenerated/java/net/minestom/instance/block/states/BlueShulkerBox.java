package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlueShulkerBox {
	public static void initStates() {
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8808, "facing=north"));
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8809, "facing=east"));
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8810, "facing=south"));
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8811, "facing=west"));
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8812, "facing=up"));
		BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8813, "facing=down"));
	}
}
