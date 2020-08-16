package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class MagentaShulkerBox {
	public static void initStates() {
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9294, "facing=north"));
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9295, "facing=east"));
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9296, "facing=south"));
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9297, "facing=west"));
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9298, "facing=up"));
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9299, "facing=down"));
	}
}
