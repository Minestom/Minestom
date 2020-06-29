package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedBirchWood {
	public static void initStates() {
		STRIPPED_BIRCH_WOOD.addBlockAlternative(new BlockAlternative((short) 133, "axis=x"));
		STRIPPED_BIRCH_WOOD.addBlockAlternative(new BlockAlternative((short) 134, "axis=y"));
		STRIPPED_BIRCH_WOOD.addBlockAlternative(new BlockAlternative((short) 135, "axis=z"));
	}
}
