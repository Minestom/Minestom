package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class LightGrayShulkerBox {
	public static void initStates() {
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8790, "facing=north"));
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8791, "facing=east"));
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8792, "facing=south"));
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8793, "facing=west"));
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8794, "facing=up"));
		LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8795, "facing=down"));
	}
}
