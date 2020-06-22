package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlueWallBanner {
	public static void initStates() {
		BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7661, "facing=north"));
		BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7662, "facing=south"));
		BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7663, "facing=west"));
		BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7664, "facing=east"));
	}
}
