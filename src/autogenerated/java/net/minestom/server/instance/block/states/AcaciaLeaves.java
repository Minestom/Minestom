package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class AcaciaLeaves {
	public static void initStates() {
		ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 201, "distance=1", "persistent=true"));
		ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 202, "distance=1", "persistent=false"));
		ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 203, "distance=2", "persistent=true"));
		ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 204, "distance=2", "persistent=false"));
		ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 205, "distance=3", "persistent=true"));
		ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 206, "distance=3", "persistent=false"));
		ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 207, "distance=4", "persistent=true"));
		ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 208, "distance=4", "persistent=false"));
		ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 209, "distance=5", "persistent=true"));
		ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 210, "distance=5", "persistent=false"));
		ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 211, "distance=6", "persistent=true"));
		ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 212, "distance=6", "persistent=false"));
		ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 213, "distance=7", "persistent=true"));
		ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 214, "distance=7", "persistent=false"));
	}
}
