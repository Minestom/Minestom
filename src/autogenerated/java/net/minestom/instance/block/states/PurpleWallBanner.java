package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PurpleWallBanner {
	public static void initStates() {
		PURPLE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7657, "facing=north"));
		PURPLE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7658, "facing=south"));
		PURPLE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7659, "facing=west"));
		PURPLE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7660, "facing=east"));
	}
}
