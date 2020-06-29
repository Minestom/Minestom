package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SpruceLeaves {
	public static void initStates() {
		SPRUCE_LEAVES.addBlockAlternative(new BlockAlternative((short) 159, "distance=1", "persistent=true"));
		SPRUCE_LEAVES.addBlockAlternative(new BlockAlternative((short) 160, "distance=1", "persistent=false"));
		SPRUCE_LEAVES.addBlockAlternative(new BlockAlternative((short) 161, "distance=2", "persistent=true"));
		SPRUCE_LEAVES.addBlockAlternative(new BlockAlternative((short) 162, "distance=2", "persistent=false"));
		SPRUCE_LEAVES.addBlockAlternative(new BlockAlternative((short) 163, "distance=3", "persistent=true"));
		SPRUCE_LEAVES.addBlockAlternative(new BlockAlternative((short) 164, "distance=3", "persistent=false"));
		SPRUCE_LEAVES.addBlockAlternative(new BlockAlternative((short) 165, "distance=4", "persistent=true"));
		SPRUCE_LEAVES.addBlockAlternative(new BlockAlternative((short) 166, "distance=4", "persistent=false"));
		SPRUCE_LEAVES.addBlockAlternative(new BlockAlternative((short) 167, "distance=5", "persistent=true"));
		SPRUCE_LEAVES.addBlockAlternative(new BlockAlternative((short) 168, "distance=5", "persistent=false"));
		SPRUCE_LEAVES.addBlockAlternative(new BlockAlternative((short) 169, "distance=6", "persistent=true"));
		SPRUCE_LEAVES.addBlockAlternative(new BlockAlternative((short) 170, "distance=6", "persistent=false"));
		SPRUCE_LEAVES.addBlockAlternative(new BlockAlternative((short) 171, "distance=7", "persistent=true"));
		SPRUCE_LEAVES.addBlockAlternative(new BlockAlternative((short) 172, "distance=7", "persistent=false"));
	}
}
