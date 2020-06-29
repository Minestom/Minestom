package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlackShulkerBox {
	public static void initStates() {
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9368, "facing=north"));
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9369, "facing=east"));
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9370, "facing=south"));
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9371, "facing=west"));
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9372, "facing=up"));
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9373, "facing=down"));
	}
}
