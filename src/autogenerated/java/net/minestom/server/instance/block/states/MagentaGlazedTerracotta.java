package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class MagentaGlazedTerracotta {
	public static void initStates() {
		MAGENTA_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9386, "facing=north"));
		MAGENTA_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9387, "facing=south"));
		MAGENTA_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9388, "facing=west"));
		MAGENTA_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9389, "facing=east"));
	}
}
