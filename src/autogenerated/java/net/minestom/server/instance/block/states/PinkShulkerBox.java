package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PinkShulkerBox {
	public static void initStates() {
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9318, "facing=north"));
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9319, "facing=east"));
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9320, "facing=south"));
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9321, "facing=west"));
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9322, "facing=up"));
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9323, "facing=down"));
	}
}
