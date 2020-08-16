package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class LightGrayShulkerBox {
	public static void initStates() {
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9330, "facing=north"));
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9331, "facing=east"));
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9332, "facing=south"));
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9333, "facing=west"));
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9334, "facing=up"));
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9335, "facing=down"));
	}
}
