package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CyanWallBanner {
	public static void initStates() {
		CYAN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7653, "facing=north"));
		CYAN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7654, "facing=south"));
		CYAN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7655, "facing=west"));
		CYAN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7656, "facing=east"));
	}
}
