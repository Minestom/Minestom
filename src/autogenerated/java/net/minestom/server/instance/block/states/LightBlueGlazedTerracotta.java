package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class LightBlueGlazedTerracotta {
	public static void initStates() {
		LIGHT_BLUE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9390, "facing=north"));
		LIGHT_BLUE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9391, "facing=south"));
		LIGHT_BLUE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9392, "facing=west"));
		LIGHT_BLUE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9393, "facing=east"));
	}
}
