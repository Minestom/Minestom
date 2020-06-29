package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PurpleShulkerBox {
	public static void initStates() {
		PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9338, "facing=north"));
		PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9339, "facing=east"));
		PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9340, "facing=south"));
		PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9341, "facing=west"));
		PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9342, "facing=up"));
		PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9343, "facing=down"));
	}
}
