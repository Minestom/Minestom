package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GrayGlazedTerracotta {
	public static void initStates() {
		GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9402, "facing=north"));
		GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9403, "facing=south"));
		GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9404, "facing=west"));
		GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9405, "facing=east"));
	}
}
