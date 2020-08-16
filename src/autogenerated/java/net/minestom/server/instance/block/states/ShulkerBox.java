package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class ShulkerBox {
	public static void initStates() {
		SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9276, "facing=north"));
		SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9277, "facing=east"));
		SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9278, "facing=south"));
		SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9279, "facing=west"));
		SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9280, "facing=up"));
		SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9281, "facing=down"));
	}
}
