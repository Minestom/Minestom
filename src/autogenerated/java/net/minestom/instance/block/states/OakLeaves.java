package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OakLeaves {
	public static void initStates() {
		OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 144, "distance=1", "persistent=true"));
		OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 145, "distance=1", "persistent=false"));
		OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 146, "distance=2", "persistent=true"));
		OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 147, "distance=2", "persistent=false"));
		OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 148, "distance=3", "persistent=true"));
		OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 149, "distance=3", "persistent=false"));
		OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 150, "distance=4", "persistent=true"));
		OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 151, "distance=4", "persistent=false"));
		OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 152, "distance=5", "persistent=true"));
		OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 153, "distance=5", "persistent=false"));
		OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 154, "distance=6", "persistent=true"));
		OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 155, "distance=6", "persistent=false"));
		OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 156, "distance=7", "persistent=true"));
		OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 157, "distance=7", "persistent=false"));
	}
}
