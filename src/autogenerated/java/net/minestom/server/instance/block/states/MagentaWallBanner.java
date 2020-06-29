package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class MagentaWallBanner {
	public static void initStates() {
		MAGENTA_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8161, "facing=north"));
		MAGENTA_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8162, "facing=south"));
		MAGENTA_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8163, "facing=west"));
		MAGENTA_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8164, "facing=east"));
	}
}
