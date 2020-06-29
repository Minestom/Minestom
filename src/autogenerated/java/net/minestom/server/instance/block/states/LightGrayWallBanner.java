package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class LightGrayWallBanner {
	public static void initStates() {
		LIGHT_GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8185, "facing=north"));
		LIGHT_GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8186, "facing=south"));
		LIGHT_GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8187, "facing=west"));
		LIGHT_GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8188, "facing=east"));
	}
}
