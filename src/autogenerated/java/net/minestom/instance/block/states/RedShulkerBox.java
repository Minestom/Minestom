package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class RedShulkerBox {
	public static void initStates() {
		RED_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8826, "facing=north"));
		RED_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8827, "facing=east"));
		RED_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8828, "facing=south"));
		RED_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8829, "facing=west"));
		RED_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8830, "facing=up"));
		RED_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8831, "facing=down"));
	}
}
