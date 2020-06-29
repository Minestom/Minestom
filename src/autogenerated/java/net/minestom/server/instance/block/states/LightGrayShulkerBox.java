package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class LightGrayShulkerBox {
	public static void initStates() {
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9326, "facing=north"));
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9327, "facing=east"));
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9328, "facing=south"));
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9329, "facing=west"));
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9330, "facing=up"));
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9331, "facing=down"));
	}
}
