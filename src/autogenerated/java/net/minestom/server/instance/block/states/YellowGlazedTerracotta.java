package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class YellowGlazedTerracotta {
	public static void initStates() {
		YELLOW_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9394, "facing=north"));
		YELLOW_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9395, "facing=south"));
		YELLOW_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9396, "facing=west"));
		YELLOW_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9397, "facing=east"));
	}
}
