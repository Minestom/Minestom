package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class MagentaShulkerBox {
	public static void initStates() {
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8754, "facing=north"));
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8755, "facing=east"));
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8756, "facing=south"));
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8757, "facing=west"));
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8758, "facing=up"));
		MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8759, "facing=down"));
	}
}
