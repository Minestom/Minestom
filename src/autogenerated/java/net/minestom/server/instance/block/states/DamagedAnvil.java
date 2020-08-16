package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DamagedAnvil {
	public static void initStates() {
		DAMAGED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6622, "facing=north"));
		DAMAGED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6623, "facing=south"));
		DAMAGED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6624, "facing=west"));
		DAMAGED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6625, "facing=east"));
	}
}
