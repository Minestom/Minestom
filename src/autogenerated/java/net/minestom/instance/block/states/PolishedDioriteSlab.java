package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PolishedDioriteSlab {
	public static void initStates() {
		POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10271, "type=top", "waterlogged=true"));
		POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10272, "type=top", "waterlogged=false"));
		POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10273, "type=bottom", "waterlogged=true"));
		POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10274, "type=bottom", "waterlogged=false"));
		POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10275, "type=double", "waterlogged=true"));
		POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10276, "type=double", "waterlogged=false"));
	}
}
