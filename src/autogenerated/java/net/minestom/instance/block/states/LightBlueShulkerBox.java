package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class LightBlueShulkerBox {
	public static void initStates() {
		LIGHT_BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8760, "facing=north"));
		LIGHT_BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8761, "facing=east"));
		LIGHT_BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8762, "facing=south"));
		LIGHT_BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8763, "facing=west"));
		LIGHT_BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8764, "facing=up"));
		LIGHT_BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8765, "facing=down"));
	}
}
