package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SkeletonWallSkull {
	public static void initStates() {
		SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 6510, "facing=north"));
		SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 6511, "facing=south"));
		SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 6512, "facing=west"));
		SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 6513, "facing=east"));
	}
}
