package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PinkWallBanner {
	public static void initStates() {
		PINK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7641, "facing=north"));
		PINK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7642, "facing=south"));
		PINK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7643, "facing=west"));
		PINK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7644, "facing=east"));
	}
}
