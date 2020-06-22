package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class MagentaGlazedTerracotta {
	public static void initStates() {
		MAGENTA_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8846, "facing=north"));
		MAGENTA_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8847, "facing=south"));
		MAGENTA_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8848, "facing=west"));
		MAGENTA_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8849, "facing=east"));
	}
}
