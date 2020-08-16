package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlackShulkerBox {
	public static void initStates() {
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9372, "facing=north"));
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9373, "facing=east"));
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9374, "facing=south"));
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9375, "facing=west"));
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9376, "facing=up"));
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9377, "facing=down"));
	}
}
