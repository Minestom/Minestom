package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class LightBlueWallBanner {
	public static void initStates() {
		LIGHT_BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7629, "facing=north"));
		LIGHT_BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7630, "facing=south"));
		LIGHT_BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7631, "facing=west"));
		LIGHT_BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7632, "facing=east"));
	}
}
