package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GreenWallBanner {
	public static void initStates() {
		GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7669, "facing=north"));
		GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7670, "facing=south"));
		GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7671, "facing=west"));
		GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7672, "facing=east"));
	}
}
