package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BrownGlazedTerracotta {
	public static void initStates() {
		BROWN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8886, "facing=north"));
		BROWN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8887, "facing=south"));
		BROWN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8888, "facing=west"));
		BROWN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8889, "facing=east"));
	}
}
