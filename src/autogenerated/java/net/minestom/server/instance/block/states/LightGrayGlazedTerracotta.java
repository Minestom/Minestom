package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class LightGrayGlazedTerracotta {
	public static void initStates() {
		LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9406, "facing=north"));
		LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9407, "facing=south"));
		LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9408, "facing=west"));
		LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9409, "facing=east"));
	}
}
