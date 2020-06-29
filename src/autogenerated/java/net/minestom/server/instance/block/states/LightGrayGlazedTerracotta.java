package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class LightGrayGlazedTerracotta {
	public static void initStates() {
		LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8870, "facing=north"));
		LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8871, "facing=south"));
		LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8872, "facing=west"));
		LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8873, "facing=east"));
	}
}
