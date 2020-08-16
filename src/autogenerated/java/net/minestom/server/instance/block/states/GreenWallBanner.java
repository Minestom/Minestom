package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GreenWallBanner {
	public static void initStates() {
		GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8209, "facing=north"));
		GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8210, "facing=south"));
		GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8211, "facing=west"));
		GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8212, "facing=east"));
	}
}
