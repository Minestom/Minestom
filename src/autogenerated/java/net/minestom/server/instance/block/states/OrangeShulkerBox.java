package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OrangeShulkerBox {
	public static void initStates() {
		ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9284, "facing=north"));
		ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9285, "facing=east"));
		ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9286, "facing=south"));
		ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9287, "facing=west"));
		ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9288, "facing=up"));
		ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9289, "facing=down"));
	}
}
