package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class WallTorch {
	public static void initStates() {
		WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 1436, "facing=north"));
		WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 1437, "facing=south"));
		WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 1438, "facing=west"));
		WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 1439, "facing=east"));
	}
}
