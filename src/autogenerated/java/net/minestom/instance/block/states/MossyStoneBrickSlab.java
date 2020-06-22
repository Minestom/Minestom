package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class MossyStoneBrickSlab {
	public static void initStates() {
		MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10265, "type=top", "waterlogged=true"));
		MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10266, "type=top", "waterlogged=false"));
		MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10267, "type=bottom", "waterlogged=true"));
		MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10268, "type=bottom", "waterlogged=false"));
		MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10269, "type=double", "waterlogged=true"));
		MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10270, "type=double", "waterlogged=false"));
	}
}
