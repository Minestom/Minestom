package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class RedNetherBrickSlab {
	public static void initStates() {
		RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10853, "type=top", "waterlogged=true"));
		RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10854, "type=top", "waterlogged=false"));
		RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10855, "type=bottom", "waterlogged=true"));
		RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10856, "type=bottom", "waterlogged=false"));
		RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10857, "type=double", "waterlogged=true"));
		RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10858, "type=double", "waterlogged=false"));
	}
}
