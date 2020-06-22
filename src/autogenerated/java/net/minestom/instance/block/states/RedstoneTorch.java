package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class RedstoneTorch {
	public static void initStates() {
		REDSTONE_TORCH.addBlockAlternative(new BlockAlternative((short) 3885, "lit=true"));
		REDSTONE_TORCH.addBlockAlternative(new BlockAlternative((short) 3886, "lit=false"));
	}
}
