package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BirchWallSign {
	public static void initStates() {
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3751, "facing=north", "waterlogged=true"));
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3752, "facing=north", "waterlogged=false"));
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3753, "facing=south", "waterlogged=true"));
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3754, "facing=south", "waterlogged=false"));
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3755, "facing=west", "waterlogged=true"));
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3756, "facing=west", "waterlogged=false"));
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3757, "facing=east", "waterlogged=true"));
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3758, "facing=east", "waterlogged=false"));
	}
}
