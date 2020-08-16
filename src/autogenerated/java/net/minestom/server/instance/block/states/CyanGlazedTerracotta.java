package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CyanGlazedTerracotta {
	public static void initStates() {
		CYAN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9414, "facing=north"));
		CYAN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9415, "facing=south"));
		CYAN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9416, "facing=west"));
		CYAN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9417, "facing=east"));
	}
}
