package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PurpleGlazedTerracotta {
	public static void initStates() {
		PURPLE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9414, "facing=north"));
		PURPLE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9415, "facing=south"));
		PURPLE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9416, "facing=west"));
		PURPLE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9417, "facing=east"));
	}
}
