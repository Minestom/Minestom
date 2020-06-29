package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class WhiteGlazedTerracotta {
	public static void initStates() {
		WHITE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9374, "facing=north"));
		WHITE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9375, "facing=south"));
		WHITE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9376, "facing=west"));
		WHITE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9377, "facing=east"));
	}
}
