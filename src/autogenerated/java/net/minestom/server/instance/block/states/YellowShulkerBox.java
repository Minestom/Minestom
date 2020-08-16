package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class YellowShulkerBox {
	public static void initStates() {
		YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9306, "facing=north"));
		YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9307, "facing=east"));
		YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9308, "facing=south"));
		YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9309, "facing=west"));
		YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9310, "facing=up"));
		YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9311, "facing=down"));
	}
}
