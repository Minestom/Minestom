package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class MagentaWallBanner {
	public static void initStates() {
		MAGENTA_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7625, "facing=north"));
		MAGENTA_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7626, "facing=south"));
		MAGENTA_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7627, "facing=west"));
		MAGENTA_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7628, "facing=east"));
	}
}
