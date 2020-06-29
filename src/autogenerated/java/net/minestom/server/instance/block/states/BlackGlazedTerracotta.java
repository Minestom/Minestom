package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlackGlazedTerracotta {
	public static void initStates() {
		BLACK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9434, "facing=north"));
		BLACK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9435, "facing=south"));
		BLACK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9436, "facing=west"));
		BLACK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9437, "facing=east"));
	}
}
