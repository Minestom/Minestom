package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class TallGrass {
	public static void initStates() {
		TALL_GRASS.addBlockAlternative(new BlockAlternative((short) 7357, "half=upper"));
		TALL_GRASS.addBlockAlternative(new BlockAlternative((short) 7358, "half=lower"));
	}
}
