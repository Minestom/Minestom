package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CutRedSandstoneSlab {
	public static void initStates() {
		CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8402, "type=top", "waterlogged=true"));
		CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8403, "type=top", "waterlogged=false"));
		CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8404, "type=bottom", "waterlogged=true"));
		CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8405, "type=bottom", "waterlogged=false"));
		CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8406, "type=double", "waterlogged=true"));
		CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8407, "type=double", "waterlogged=false"));
	}
}
