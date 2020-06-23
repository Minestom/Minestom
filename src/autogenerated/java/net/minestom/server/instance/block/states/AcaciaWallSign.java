package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class AcaciaWallSign {
	public static void initStates() {
		ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3757, "facing=north", "waterlogged=true"));
		ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3758, "facing=north", "waterlogged=false"));
		ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3759, "facing=south", "waterlogged=true"));
		ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3760, "facing=south", "waterlogged=false"));
		ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3761, "facing=west", "waterlogged=true"));
		ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3762, "facing=west", "waterlogged=false"));
		ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3763, "facing=east", "waterlogged=true"));
		ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3764, "facing=east", "waterlogged=false"));
	}
}
