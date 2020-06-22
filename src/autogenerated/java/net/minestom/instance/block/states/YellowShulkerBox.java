package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class YellowShulkerBox {
	public static void initStates() {
		YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8766, "facing=north"));
		YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8767, "facing=east"));
		YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8768, "facing=south"));
		YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8769, "facing=west"));
		YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8770, "facing=up"));
		YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8771, "facing=down"));
	}
}
