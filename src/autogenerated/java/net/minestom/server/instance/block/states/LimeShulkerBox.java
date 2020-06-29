package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class LimeShulkerBox {
	public static void initStates() {
		LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9308, "facing=north"));
		LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9309, "facing=east"));
		LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9310, "facing=south"));
		LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9311, "facing=west"));
		LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9312, "facing=up"));
		LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9313, "facing=down"));
	}
}
