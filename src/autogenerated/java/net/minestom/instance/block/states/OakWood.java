package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OakWood {
	public static void initStates() {
		OAK_WOOD.addBlockAlternative(new BlockAlternative((short) 108, "axis=x"));
		OAK_WOOD.addBlockAlternative(new BlockAlternative((short) 109, "axis=y"));
		OAK_WOOD.addBlockAlternative(new BlockAlternative((short) 110, "axis=z"));
	}
}
