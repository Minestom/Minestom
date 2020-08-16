package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class RedGlazedTerracotta {
	public static void initStates() {
		RED_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9434, "facing=north"));
		RED_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9435, "facing=south"));
		RED_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9436, "facing=west"));
		RED_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9437, "facing=east"));
	}
}
