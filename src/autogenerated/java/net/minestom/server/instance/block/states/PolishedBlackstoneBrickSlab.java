package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PolishedBlackstoneBrickSlab {
	public static void initStates() {
		POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16262, "type=top", "waterlogged=true"));
		POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16263, "type=top", "waterlogged=false"));
		POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16264, "type=bottom", "waterlogged=true"));
		POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16265, "type=bottom", "waterlogged=false"));
		POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16266, "type=double", "waterlogged=true"));
		POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16267, "type=double", "waterlogged=false"));
	}
}
