package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SmoothRedSandstoneSlab {
	public static void initStates() {
		SMOOTH_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10259, "type=top", "waterlogged=true"));
		SMOOTH_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10260, "type=top", "waterlogged=false"));
		SMOOTH_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10261, "type=bottom", "waterlogged=true"));
		SMOOTH_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10262, "type=bottom", "waterlogged=false"));
		SMOOTH_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10263, "type=double", "waterlogged=true"));
		SMOOTH_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10264, "type=double", "waterlogged=false"));
	}
}
