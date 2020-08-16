package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class ChippedAnvil {
	public static void initStates() {
		CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6618, "facing=north"));
		CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6619, "facing=south"));
		CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6620, "facing=west"));
		CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6621, "facing=east"));
	}
}
