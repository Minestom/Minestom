package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BirchWallSign {
	public static void initStates() {
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3749, "facing=north", "waterlogged=true"));
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3750, "facing=north", "waterlogged=false"));
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3751, "facing=south", "waterlogged=true"));
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3752, "facing=south", "waterlogged=false"));
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3753, "facing=west", "waterlogged=true"));
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3754, "facing=west", "waterlogged=false"));
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3755, "facing=east", "waterlogged=true"));
		BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3756, "facing=east", "waterlogged=false"));
	}
}
