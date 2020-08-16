package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class EndStoneBrickSlab {
	public static void initStates() {
		END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10823, "type=top", "waterlogged=true"));
		END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10824, "type=top", "waterlogged=false"));
		END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10825, "type=bottom", "waterlogged=true"));
		END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10826, "type=bottom", "waterlogged=false"));
		END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10827, "type=double", "waterlogged=true"));
		END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10828, "type=double", "waterlogged=false"));
	}
}
