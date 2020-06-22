package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BirchSlab {
	public static void initStates() {
		BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 7776, "type=top", "waterlogged=true"));
		BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 7777, "type=top", "waterlogged=false"));
		BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 7778, "type=bottom", "waterlogged=true"));
		BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 7779, "type=bottom", "waterlogged=false"));
		BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 7780, "type=double", "waterlogged=true"));
		BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 7781, "type=double", "waterlogged=false"));
	}
}
