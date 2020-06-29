package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GrayWallBanner {
	public static void initStates() {
		GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8181, "facing=north"));
		GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8182, "facing=south"));
		GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8183, "facing=west"));
		GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8184, "facing=east"));
	}
}
