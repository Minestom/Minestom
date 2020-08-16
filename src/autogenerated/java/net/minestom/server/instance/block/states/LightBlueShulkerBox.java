package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class LightBlueShulkerBox {
	public static void initStates() {
		LIGHT_BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9300, "facing=north"));
		LIGHT_BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9301, "facing=east"));
		LIGHT_BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9302, "facing=south"));
		LIGHT_BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9303, "facing=west"));
		LIGHT_BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9304, "facing=up"));
		LIGHT_BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9305, "facing=down"));
	}
}
