package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DamagedAnvil {
	public static void initStates() {
		DAMAGED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6082, "facing=north"));
		DAMAGED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6083, "facing=south"));
		DAMAGED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6084, "facing=west"));
		DAMAGED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6085, "facing=east"));
	}
}
