package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StoneSlab {
	public static void initStates() {
		STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8340, "type=top", "waterlogged=true"));
		STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8341, "type=top", "waterlogged=false"));
		STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8342, "type=bottom", "waterlogged=true"));
		STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8343, "type=bottom", "waterlogged=false"));
		STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8344, "type=double", "waterlogged=true"));
		STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8345, "type=double", "waterlogged=false"));
	}
}
