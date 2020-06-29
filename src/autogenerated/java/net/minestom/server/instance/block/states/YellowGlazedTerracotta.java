package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class YellowGlazedTerracotta {
	public static void initStates() {
		YELLOW_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8854, "facing=north"));
		YELLOW_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8855, "facing=south"));
		YELLOW_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8856, "facing=west"));
		YELLOW_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8857, "facing=east"));
	}
}
