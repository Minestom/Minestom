package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class TrappedChest {
	public static void initStates() {
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6086, "facing=north", "type=single", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6087, "facing=north", "type=single", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6088, "facing=north", "type=left", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6089, "facing=north", "type=left", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6090, "facing=north", "type=right", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6091, "facing=north", "type=right", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6092, "facing=south", "type=single", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6093, "facing=south", "type=single", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6094, "facing=south", "type=left", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6095, "facing=south", "type=left", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6096, "facing=south", "type=right", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6097, "facing=south", "type=right", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6098, "facing=west", "type=single", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6099, "facing=west", "type=single", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6100, "facing=west", "type=left", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6101, "facing=west", "type=left", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6102, "facing=west", "type=right", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6103, "facing=west", "type=right", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6104, "facing=east", "type=single", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6105, "facing=east", "type=single", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6106, "facing=east", "type=left", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6107, "facing=east", "type=left", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6108, "facing=east", "type=right", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6109, "facing=east", "type=right", "waterlogged=false"));
	}
}
