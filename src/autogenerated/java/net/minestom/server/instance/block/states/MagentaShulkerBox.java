package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class MagentaShulkerBox {
	public static void initStates() {
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9290, "facing=north"));
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9291, "facing=east"));
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9292, "facing=south"));
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9293, "facing=west"));
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9294, "facing=up"));
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9295, "facing=down"));
	}
}
