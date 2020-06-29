package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PinkShulkerBox {
	public static void initStates() {
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9314, "facing=north"));
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9315, "facing=east"));
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9316, "facing=south"));
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9317, "facing=west"));
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9318, "facing=up"));
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9319, "facing=down"));
	}
}
