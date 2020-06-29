package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class ShulkerBox {
	public static void initStates() {
		SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8736, "facing=north"));
		SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8737, "facing=east"));
		SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8738, "facing=south"));
		SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8739, "facing=west"));
		SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8740, "facing=up"));
		SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8741, "facing=down"));
	}
}
