package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Anvil {
	public static void initStates() {
		ANVIL.addBlockAlternative(new BlockAlternative((short) 6614, "facing=north"));
		ANVIL.addBlockAlternative(new BlockAlternative((short) 6615, "facing=south"));
		ANVIL.addBlockAlternative(new BlockAlternative((short) 6616, "facing=west"));
		ANVIL.addBlockAlternative(new BlockAlternative((short) 6617, "facing=east"));
	}
}
