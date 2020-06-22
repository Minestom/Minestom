package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SkeletonWallSkull {
	public static void initStates() {
		SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 5970, "facing=north"));
		SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 5971, "facing=south"));
		SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 5972, "facing=west"));
		SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 5973, "facing=east"));
	}
}
