package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CyanShulkerBox {
	public static void initStates() {
		CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8796, "facing=north"));
		CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8797, "facing=east"));
		CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8798, "facing=south"));
		CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8799, "facing=west"));
		CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8800, "facing=up"));
		CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8801, "facing=down"));
	}
}
