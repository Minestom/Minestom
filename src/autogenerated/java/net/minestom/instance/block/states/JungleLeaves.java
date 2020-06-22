package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class JungleLeaves {
	public static void initStates() {
		JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 186, "distance=1", "persistent=true"));
		JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 187, "distance=1", "persistent=false"));
		JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 188, "distance=2", "persistent=true"));
		JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 189, "distance=2", "persistent=false"));
		JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 190, "distance=3", "persistent=true"));
		JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 191, "distance=3", "persistent=false"));
		JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 192, "distance=4", "persistent=true"));
		JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 193, "distance=4", "persistent=false"));
		JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 194, "distance=5", "persistent=true"));
		JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 195, "distance=5", "persistent=false"));
		JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 196, "distance=6", "persistent=true"));
		JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 197, "distance=6", "persistent=false"));
		JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 198, "distance=7", "persistent=true"));
		JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 199, "distance=7", "persistent=false"));
	}
}
