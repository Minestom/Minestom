package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PolishedBlackstoneBrickSlab {
	public static void initStates() {
		POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16254, "type=top", "waterlogged=true"));
		POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16255, "type=top", "waterlogged=false"));
		POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16256, "type=bottom", "waterlogged=true"));
		POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16257, "type=bottom", "waterlogged=false"));
		POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16258, "type=double", "waterlogged=true"));
		POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16259, "type=double", "waterlogged=false"));
	}
}
