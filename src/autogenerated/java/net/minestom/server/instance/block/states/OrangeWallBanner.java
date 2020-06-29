package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OrangeWallBanner {
	public static void initStates() {
		ORANGE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7621, "facing=north"));
		ORANGE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7622, "facing=south"));
		ORANGE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7623, "facing=west"));
		ORANGE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7624, "facing=east"));
	}
}
