package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SmoothStoneSlab {
	public static void initStates() {
		SMOOTH_STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8346, "type=top", "waterlogged=true"));
		SMOOTH_STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8347, "type=top", "waterlogged=false"));
		SMOOTH_STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8348, "type=bottom", "waterlogged=true"));
		SMOOTH_STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8349, "type=bottom", "waterlogged=false"));
		SMOOTH_STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8350, "type=double", "waterlogged=true"));
		SMOOTH_STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8351, "type=double", "waterlogged=false"));
	}
}
