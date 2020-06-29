package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CyanWallBanner {
	public static void initStates() {
		CYAN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8189, "facing=north"));
		CYAN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8190, "facing=south"));
		CYAN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8191, "facing=west"));
		CYAN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8192, "facing=east"));
	}
}
