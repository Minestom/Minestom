package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CreeperWallHead {
	public static void initStates() {
		CREEPER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6590, "facing=north"));
		CREEPER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6591, "facing=south"));
		CREEPER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6592, "facing=west"));
		CREEPER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6593, "facing=east"));
	}
}
