package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CrimsonWallSign {
	public static void initStates() {
		CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15719, "facing=north", "waterlogged=true"));
		CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15720, "facing=north", "waterlogged=false"));
		CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15721, "facing=south", "waterlogged=true"));
		CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15722, "facing=south", "waterlogged=false"));
		CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15723, "facing=west", "waterlogged=true"));
		CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15724, "facing=west", "waterlogged=false"));
		CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15725, "facing=east", "waterlogged=true"));
		CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15726, "facing=east", "waterlogged=false"));
	}
}
