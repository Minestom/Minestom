package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlackWallBanner {
	public static void initStates() {
		BLACK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8213, "facing=north"));
		BLACK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8214, "facing=south"));
		BLACK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8215, "facing=west"));
		BLACK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8216, "facing=east"));
	}
}
