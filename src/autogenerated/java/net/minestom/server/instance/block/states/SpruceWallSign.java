package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SpruceWallSign {
	public static void initStates() {
		SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3743, "facing=north", "waterlogged=true"));
		SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3744, "facing=north", "waterlogged=false"));
		SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3745, "facing=south", "waterlogged=true"));
		SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3746, "facing=south", "waterlogged=false"));
		SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3747, "facing=west", "waterlogged=true"));
		SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3748, "facing=west", "waterlogged=false"));
		SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3749, "facing=east", "waterlogged=true"));
		SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3750, "facing=east", "waterlogged=false"));
	}
}
