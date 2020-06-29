package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class ChippedAnvil {
	public static void initStates() {
		CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6614, "facing=north"));
		CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6615, "facing=south"));
		CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6616, "facing=west"));
		CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6617, "facing=east"));
	}
}
