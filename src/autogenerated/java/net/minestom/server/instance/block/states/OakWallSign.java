package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OakWallSign {
	public static void initStates() {
		OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3735, "facing=north", "waterlogged=true"));
		OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3736, "facing=north", "waterlogged=false"));
		OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3737, "facing=south", "waterlogged=true"));
		OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3738, "facing=south", "waterlogged=false"));
		OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3739, "facing=west", "waterlogged=true"));
		OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3740, "facing=west", "waterlogged=false"));
		OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3741, "facing=east", "waterlogged=true"));
		OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3742, "facing=east", "waterlogged=false"));
	}
}
