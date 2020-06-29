package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedSpruceWood {
	public static void initStates() {
		STRIPPED_SPRUCE_WOOD.addBlockAlternative(new BlockAlternative((short) 130, "axis=x"));
		STRIPPED_SPRUCE_WOOD.addBlockAlternative(new BlockAlternative((short) 131, "axis=y"));
		STRIPPED_SPRUCE_WOOD.addBlockAlternative(new BlockAlternative((short) 132, "axis=z"));
	}
}
