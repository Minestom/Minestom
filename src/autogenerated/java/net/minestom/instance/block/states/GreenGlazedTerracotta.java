package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GreenGlazedTerracotta {
	public static void initStates() {
		GREEN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8890, "facing=north"));
		GREEN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8891, "facing=south"));
		GREEN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8892, "facing=west"));
		GREEN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8893, "facing=east"));
	}
}
