package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class ZombieWallHead {
	public static void initStates() {
		ZOMBIE_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6546, "facing=north"));
		ZOMBIE_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6547, "facing=south"));
		ZOMBIE_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6548, "facing=west"));
		ZOMBIE_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6549, "facing=east"));
	}
}
