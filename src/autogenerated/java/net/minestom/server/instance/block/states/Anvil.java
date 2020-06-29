package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Anvil {
	public static void initStates() {
		ANVIL.addBlockAlternative(new BlockAlternative((short) 6610, "facing=north"));
		ANVIL.addBlockAlternative(new BlockAlternative((short) 6611, "facing=south"));
		ANVIL.addBlockAlternative(new BlockAlternative((short) 6612, "facing=west"));
		ANVIL.addBlockAlternative(new BlockAlternative((short) 6613, "facing=east"));
	}
}
