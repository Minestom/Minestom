package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DioriteSlab {
	public static void initStates() {
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10325, "type=top", "waterlogged=true"));
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10326, "type=top", "waterlogged=false"));
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10327, "type=bottom", "waterlogged=true"));
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10328, "type=bottom", "waterlogged=false"));
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10329, "type=double", "waterlogged=true"));
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10330, "type=double", "waterlogged=false"));
	}
}
