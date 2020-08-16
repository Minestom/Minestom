package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GrayGlazedTerracotta {
	public static void initStates() {
		GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9406, "facing=north"));
		GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9407, "facing=south"));
		GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9408, "facing=west"));
		GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9409, "facing=east"));
	}
}
