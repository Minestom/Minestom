package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BirchLeaves {
	public static void initStates() {
		BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 172, "distance=1", "persistent=true"));
		BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 173, "distance=1", "persistent=false"));
		BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 174, "distance=2", "persistent=true"));
		BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 175, "distance=2", "persistent=false"));
		BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 176, "distance=3", "persistent=true"));
		BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 177, "distance=3", "persistent=false"));
		BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 178, "distance=4", "persistent=true"));
		BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 179, "distance=4", "persistent=false"));
		BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 180, "distance=5", "persistent=true"));
		BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 181, "distance=5", "persistent=false"));
		BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 182, "distance=6", "persistent=true"));
		BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 183, "distance=6", "persistent=false"));
		BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 184, "distance=7", "persistent=true"));
		BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 185, "distance=7", "persistent=false"));
	}
}
