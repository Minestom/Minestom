package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Stonecutter {
	public static void initStates() {
		STONECUTTER.addBlockAlternative(new BlockAlternative((short) 14854, "facing=north"));
		STONECUTTER.addBlockAlternative(new BlockAlternative((short) 14855, "facing=south"));
		STONECUTTER.addBlockAlternative(new BlockAlternative((short) 14856, "facing=west"));
		STONECUTTER.addBlockAlternative(new BlockAlternative((short) 14857, "facing=east"));
	}
}
