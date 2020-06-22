package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlueGlazedTerracotta {
	public static void initStates() {
		BLUE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8882, "facing=north"));
		BLUE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8883, "facing=south"));
		BLUE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8884, "facing=west"));
		BLUE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8885, "facing=east"));
	}
}
