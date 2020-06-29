package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BirchWood {
	public static void initStates() {
		BIRCH_WOOD.addBlockAlternative(new BlockAlternative((short) 115, "axis=x"));
		BIRCH_WOOD.addBlockAlternative(new BlockAlternative((short) 116, "axis=y"));
		BIRCH_WOOD.addBlockAlternative(new BlockAlternative((short) 117, "axis=z"));
	}
}
