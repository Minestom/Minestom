package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PinkGlazedTerracotta {
	public static void initStates() {
		PINK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9402, "facing=north"));
		PINK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9403, "facing=south"));
		PINK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9404, "facing=west"));
		PINK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9405, "facing=east"));
	}
}
