package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StoneBrickSlab {
	public static void initStates() {
		STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8382, "type=top", "waterlogged=true"));
		STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8383, "type=top", "waterlogged=false"));
		STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8384, "type=bottom", "waterlogged=true"));
		STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8385, "type=bottom", "waterlogged=false"));
		STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8386, "type=double", "waterlogged=true"));
		STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8387, "type=double", "waterlogged=false"));
	}
}
