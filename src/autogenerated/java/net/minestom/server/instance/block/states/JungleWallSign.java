package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class JungleWallSign {
	public static void initStates() {
		JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3767, "facing=north", "waterlogged=true"));
		JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3768, "facing=north", "waterlogged=false"));
		JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3769, "facing=south", "waterlogged=true"));
		JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3770, "facing=south", "waterlogged=false"));
		JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3771, "facing=west", "waterlogged=true"));
		JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3772, "facing=west", "waterlogged=false"));
		JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3773, "facing=east", "waterlogged=true"));
		JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3774, "facing=east", "waterlogged=false"));
	}
}
