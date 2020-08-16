package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CyanShulkerBox {
	public static void initStates() {
		CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9336, "facing=north"));
		CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9337, "facing=east"));
		CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9338, "facing=south"));
		CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9339, "facing=west"));
		CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9340, "facing=up"));
		CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9341, "facing=down"));
	}
}
