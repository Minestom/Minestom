package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Anvil {
	public static void initStates() {
		ANVIL.addBlockAlternative(new BlockAlternative((short) 6074, "facing=north"));
		ANVIL.addBlockAlternative(new BlockAlternative((short) 6075, "facing=south"));
		ANVIL.addBlockAlternative(new BlockAlternative((short) 6076, "facing=west"));
		ANVIL.addBlockAlternative(new BlockAlternative((short) 6077, "facing=east"));
	}
}
