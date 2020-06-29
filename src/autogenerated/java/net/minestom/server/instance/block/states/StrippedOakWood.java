package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedOakWood {
	public static void initStates() {
		STRIPPED_OAK_WOOD.addBlockAlternative(new BlockAlternative((short) 127, "axis=x"));
		STRIPPED_OAK_WOOD.addBlockAlternative(new BlockAlternative((short) 128, "axis=y"));
		STRIPPED_OAK_WOOD.addBlockAlternative(new BlockAlternative((short) 129, "axis=z"));
	}
}
