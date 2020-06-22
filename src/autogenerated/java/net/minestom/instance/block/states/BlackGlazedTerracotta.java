package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlackGlazedTerracotta {
	public static void initStates() {
		BLACK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8898, "facing=north"));
		BLACK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8899, "facing=south"));
		BLACK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8900, "facing=west"));
		BLACK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8901, "facing=east"));
	}
}
