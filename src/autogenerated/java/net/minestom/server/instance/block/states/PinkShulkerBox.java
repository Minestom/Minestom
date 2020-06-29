package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PinkShulkerBox {
	public static void initStates() {
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8778, "facing=north"));
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8779, "facing=east"));
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8780, "facing=south"));
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8781, "facing=west"));
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8782, "facing=up"));
		PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8783, "facing=down"));
	}
}
