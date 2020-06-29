package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class RedWallBanner {
	public static void initStates() {
		RED_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8209, "facing=north"));
		RED_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8210, "facing=south"));
		RED_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8211, "facing=west"));
		RED_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8212, "facing=east"));
	}
}
