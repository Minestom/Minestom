package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlackShulkerBox {
	public static void initStates() {
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8832, "facing=north"));
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8833, "facing=east"));
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8834, "facing=south"));
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8835, "facing=west"));
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8836, "facing=up"));
		BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8837, "facing=down"));
	}
}
