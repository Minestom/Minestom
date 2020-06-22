package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class LimeShulkerBox {
	public static void initStates() {
		LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8772, "facing=north"));
		LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8773, "facing=east"));
		LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8774, "facing=south"));
		LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8775, "facing=west"));
		LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8776, "facing=up"));
		LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8777, "facing=down"));
	}
}
