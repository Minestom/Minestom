package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class LimeWallBanner {
	public static void initStates() {
		LIME_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8177, "facing=north"));
		LIME_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8178, "facing=south"));
		LIME_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8179, "facing=west"));
		LIME_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8180, "facing=east"));
	}
}
