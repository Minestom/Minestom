package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class AcaciaWood {
	public static void initStates() {
		ACACIA_WOOD.addBlockAlternative(new BlockAlternative((short) 121, "axis=x"));
		ACACIA_WOOD.addBlockAlternative(new BlockAlternative((short) 122, "axis=y"));
		ACACIA_WOOD.addBlockAlternative(new BlockAlternative((short) 123, "axis=z"));
	}
}
