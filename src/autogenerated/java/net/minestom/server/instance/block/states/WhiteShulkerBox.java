package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class WhiteShulkerBox {
	public static void initStates() {
		WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9282, "facing=north"));
		WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9283, "facing=east"));
		WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9284, "facing=south"));
		WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9285, "facing=west"));
		WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9286, "facing=up"));
		WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9287, "facing=down"));
	}
}
