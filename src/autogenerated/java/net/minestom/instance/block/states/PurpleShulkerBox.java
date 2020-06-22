package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PurpleShulkerBox {
	public static void initStates() {
		PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8802, "facing=north"));
		PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8803, "facing=east"));
		PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8804, "facing=south"));
		PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8805, "facing=west"));
		PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8806, "facing=up"));
		PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8807, "facing=down"));
	}
}
