package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class WarpedWallSign {
	public static void initStates() {
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15727, "facing=north", "waterlogged=true"));
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15728, "facing=north", "waterlogged=false"));
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15729, "facing=south", "waterlogged=true"));
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15730, "facing=south", "waterlogged=false"));
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15731, "facing=west", "waterlogged=true"));
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15732, "facing=west", "waterlogged=false"));
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15733, "facing=east", "waterlogged=true"));
		WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15734, "facing=east", "waterlogged=false"));
	}
}
