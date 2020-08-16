package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class WarpedWallSign {
	public static void initStates() {
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15735, "facing=north", "waterlogged=true"));
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15736, "facing=north", "waterlogged=false"));
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15737, "facing=south", "waterlogged=true"));
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15738, "facing=south", "waterlogged=false"));
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15739, "facing=west", "waterlogged=true"));
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15740, "facing=west", "waterlogged=false"));
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15741, "facing=east", "waterlogged=true"));
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15742, "facing=east", "waterlogged=false"));
	}
}
