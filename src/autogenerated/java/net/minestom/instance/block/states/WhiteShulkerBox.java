package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class WhiteShulkerBox {
	public static void initStates() {
		WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8742, "facing=north"));
		WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8743, "facing=east"));
		WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8744, "facing=south"));
		WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8745, "facing=west"));
		WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8746, "facing=up"));
		WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 8747, "facing=down"));
	}
}
