package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GreenShulkerBox {
	public static void initStates() {
		GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8820, "facing=north"));
		GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8821, "facing=east"));
		GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8822, "facing=south"));
		GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8823, "facing=west"));
		GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8824, "facing=up"));
		GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8825, "facing=down"));
	}
}
