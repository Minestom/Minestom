package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BrownGlazedTerracotta {
	public static void initStates() {
		BROWN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9422, "facing=north"));
		BROWN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9423, "facing=south"));
		BROWN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9424, "facing=west"));
		BROWN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9425, "facing=east"));
	}
}
