package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SpruceWood {
	public static void initStates() {
		SPRUCE_WOOD.addBlockAlternative(new BlockAlternative((short) 112, "axis=x"));
		SPRUCE_WOOD.addBlockAlternative(new BlockAlternative((short) 113, "axis=y"));
		SPRUCE_WOOD.addBlockAlternative(new BlockAlternative((short) 114, "axis=z"));
	}
}
