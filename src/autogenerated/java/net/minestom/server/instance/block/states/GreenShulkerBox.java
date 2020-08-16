package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GreenShulkerBox {
	public static void initStates() {
		GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9360, "facing=north"));
		GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9361, "facing=east"));
		GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9362, "facing=south"));
		GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9363, "facing=west"));
		GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9364, "facing=up"));
		GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9365, "facing=down"));
	}
}
