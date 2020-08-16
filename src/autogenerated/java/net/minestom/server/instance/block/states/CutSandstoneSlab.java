package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CutSandstoneSlab {
	public static void initStates() {
		CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8358, "type=top", "waterlogged=true"));
		CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8359, "type=top", "waterlogged=false"));
		CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8360, "type=bottom", "waterlogged=true"));
		CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8361, "type=bottom", "waterlogged=false"));
		CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8362, "type=double", "waterlogged=true"));
		CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8363, "type=double", "waterlogged=false"));
	}
}
