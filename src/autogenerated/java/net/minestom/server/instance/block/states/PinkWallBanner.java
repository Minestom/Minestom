package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PinkWallBanner {
	public static void initStates() {
		PINK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8177, "facing=north"));
		PINK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8178, "facing=south"));
		PINK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8179, "facing=west"));
		PINK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8180, "facing=east"));
	}
}
