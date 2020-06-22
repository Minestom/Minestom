package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CutRedSandstoneSlab {
	public static void initStates() {
		CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7866, "type=top", "waterlogged=true"));
		CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7867, "type=top", "waterlogged=false"));
		CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7868, "type=bottom", "waterlogged=true"));
		CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7869, "type=bottom", "waterlogged=false"));
		CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7870, "type=double", "waterlogged=true"));
		CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7871, "type=double", "waterlogged=false"));
	}
}
