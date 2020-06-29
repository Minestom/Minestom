package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class NetherBrickSlab {
	public static void initStates() {
		NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8384, "type=top", "waterlogged=true"));
		NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8385, "type=top", "waterlogged=false"));
		NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8386, "type=bottom", "waterlogged=true"));
		NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8387, "type=bottom", "waterlogged=false"));
		NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8388, "type=double", "waterlogged=true"));
		NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8389, "type=double", "waterlogged=false"));
	}
}
