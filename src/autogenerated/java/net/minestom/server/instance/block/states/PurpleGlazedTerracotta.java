package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PurpleGlazedTerracotta {
	public static void initStates() {
		PURPLE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8878, "facing=north"));
		PURPLE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8879, "facing=south"));
		PURPLE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8880, "facing=west"));
		PURPLE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8881, "facing=east"));
	}
}
