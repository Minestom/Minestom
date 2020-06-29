package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedDarkOakWood {
	public static void initStates() {
		STRIPPED_DARK_OAK_WOOD.addBlockAlternative(new BlockAlternative((short) 142, "axis=x"));
		STRIPPED_DARK_OAK_WOOD.addBlockAlternative(new BlockAlternative((short) 143, "axis=y"));
		STRIPPED_DARK_OAK_WOOD.addBlockAlternative(new BlockAlternative((short) 144, "axis=z"));
	}
}
