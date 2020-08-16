package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PurpleWallBanner {
	public static void initStates() {
		PURPLE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8197, "facing=north"));
		PURPLE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8198, "facing=south"));
		PURPLE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8199, "facing=west"));
		PURPLE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8200, "facing=east"));
	}
}
