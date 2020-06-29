package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SoulWallTorch {
	public static void initStates() {
		SOUL_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 4009, "facing=north"));
		SOUL_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 4010, "facing=south"));
		SOUL_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 4011, "facing=west"));
		SOUL_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 4012, "facing=east"));
	}
}
