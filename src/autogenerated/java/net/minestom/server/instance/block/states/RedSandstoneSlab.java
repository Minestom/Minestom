package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class RedSandstoneSlab {
	public static void initStates() {
		RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8400, "type=top", "waterlogged=true"));
		RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8401, "type=top", "waterlogged=false"));
		RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8402, "type=bottom", "waterlogged=true"));
		RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8403, "type=bottom", "waterlogged=false"));
		RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8404, "type=double", "waterlogged=true"));
		RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8405, "type=double", "waterlogged=false"));
	}
}
