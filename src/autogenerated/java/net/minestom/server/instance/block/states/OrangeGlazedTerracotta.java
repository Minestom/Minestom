package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OrangeGlazedTerracotta {
	public static void initStates() {
		ORANGE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9378, "facing=north"));
		ORANGE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9379, "facing=south"));
		ORANGE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9380, "facing=west"));
		ORANGE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9381, "facing=east"));
	}
}
