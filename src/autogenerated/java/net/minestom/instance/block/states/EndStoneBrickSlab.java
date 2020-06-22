package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class EndStoneBrickSlab {
	public static void initStates() {
		END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10283, "type=top", "waterlogged=true"));
		END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10284, "type=top", "waterlogged=false"));
		END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10285, "type=bottom", "waterlogged=true"));
		END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10286, "type=bottom", "waterlogged=false"));
		END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10287, "type=double", "waterlogged=true"));
		END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10288, "type=double", "waterlogged=false"));
	}
}
