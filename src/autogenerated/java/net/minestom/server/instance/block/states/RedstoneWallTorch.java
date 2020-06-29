package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class RedstoneWallTorch {
	public static void initStates() {
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3889, "facing=north", "lit=true"));
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3890, "facing=north", "lit=false"));
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3891, "facing=south", "lit=true"));
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3892, "facing=south", "lit=false"));
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3893, "facing=west", "lit=true"));
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3894, "facing=west", "lit=false"));
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3895, "facing=east", "lit=true"));
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3896, "facing=east", "lit=false"));
	}
}
