package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class WarpedSlab {
	public static void initStates() {
		WARPED_SLAB.addBlockAlternative(new BlockAlternative((short) 15053, "type=top", "waterlogged=true"));
		WARPED_SLAB.addBlockAlternative(new BlockAlternative((short) 15054, "type=top", "waterlogged=false"));
		WARPED_SLAB.addBlockAlternative(new BlockAlternative((short) 15055, "type=bottom", "waterlogged=true"));
		WARPED_SLAB.addBlockAlternative(new BlockAlternative((short) 15056, "type=bottom", "waterlogged=false"));
		WARPED_SLAB.addBlockAlternative(new BlockAlternative((short) 15057, "type=double", "waterlogged=true"));
		WARPED_SLAB.addBlockAlternative(new BlockAlternative((short) 15058, "type=double", "waterlogged=false"));
	}
}
