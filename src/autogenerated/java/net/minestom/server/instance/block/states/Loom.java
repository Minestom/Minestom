package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Loom {
	public static void initStates() {
		LOOM.addBlockAlternative(new BlockAlternative((short) 14787, "facing=north"));
		LOOM.addBlockAlternative(new BlockAlternative((short) 14788, "facing=south"));
		LOOM.addBlockAlternative(new BlockAlternative((short) 14789, "facing=west"));
		LOOM.addBlockAlternative(new BlockAlternative((short) 14790, "facing=east"));
	}
}
