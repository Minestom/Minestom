package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class RedstoneWallTorch {
	public static void initStates() {
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3887, "facing=north", "lit=true"));
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3888, "facing=north", "lit=false"));
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3889, "facing=south", "lit=true"));
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3890, "facing=south", "lit=false"));
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3891, "facing=west", "lit=true"));
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3892, "facing=west", "lit=false"));
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3893, "facing=east", "lit=true"));
		REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3894, "facing=east", "lit=false"));
	}
}
