package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GreenGlazedTerracotta {
	public static void initStates() {
		GREEN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9426, "facing=north"));
		GREEN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9427, "facing=south"));
		GREEN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9428, "facing=west"));
		GREEN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9429, "facing=east"));
	}
}
