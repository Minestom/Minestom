package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GreenWallBanner {
	public static void initStates() {
		GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8205, "facing=north"));
		GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8206, "facing=south"));
		GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8207, "facing=west"));
		GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8208, "facing=east"));
	}
}
