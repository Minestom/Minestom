package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class RedWallBanner {
	public static void initStates() {
		RED_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7673, "facing=north"));
		RED_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7674, "facing=south"));
		RED_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7675, "facing=west"));
		RED_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7676, "facing=east"));
	}
}
