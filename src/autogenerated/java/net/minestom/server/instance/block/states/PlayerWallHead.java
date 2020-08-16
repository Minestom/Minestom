package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PlayerWallHead {
	public static void initStates() {
		PLAYER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6570, "facing=north"));
		PLAYER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6571, "facing=south"));
		PLAYER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6572, "facing=west"));
		PLAYER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6573, "facing=east"));
	}
}
