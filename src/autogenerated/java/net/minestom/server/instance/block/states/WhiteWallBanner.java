package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class WhiteWallBanner {
	public static void initStates() {
		WHITE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7617, "facing=north"));
		WHITE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7618, "facing=south"));
		WHITE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7619, "facing=west"));
		WHITE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7620, "facing=east"));
	}
}
