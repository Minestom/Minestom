package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CyanGlazedTerracotta {
	public static void initStates() {
		CYAN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8874, "facing=north"));
		CYAN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8875, "facing=south"));
		CYAN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8876, "facing=west"));
		CYAN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8877, "facing=east"));
	}
}
