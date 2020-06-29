package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OrangeShulkerBox {
	public static void initStates() {
		ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8748, "facing=north"));
		ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8749, "facing=east"));
		ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8750, "facing=south"));
		ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8751, "facing=west"));
		ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8752, "facing=up"));
		ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8753, "facing=down"));
	}
}
