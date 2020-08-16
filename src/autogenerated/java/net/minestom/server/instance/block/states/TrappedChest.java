package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class TrappedChest {
	public static void initStates() {
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6626, "facing=north", "type=single", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6627, "facing=north", "type=single", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6628, "facing=north", "type=left", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6629, "facing=north", "type=left", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6630, "facing=north", "type=right", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6631, "facing=north", "type=right", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6632, "facing=south", "type=single", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6633, "facing=south", "type=single", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6634, "facing=south", "type=left", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6635, "facing=south", "type=left", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6636, "facing=south", "type=right", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6637, "facing=south", "type=right", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6638, "facing=west", "type=single", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6639, "facing=west", "type=single", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6640, "facing=west", "type=left", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6641, "facing=west", "type=left", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6642, "facing=west", "type=right", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6643, "facing=west", "type=right", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6644, "facing=east", "type=single", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6645, "facing=east", "type=single", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6646, "facing=east", "type=left", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6647, "facing=east", "type=left", "waterlogged=false"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6648, "facing=east", "type=right", "waterlogged=true"));
		TRAPPED_CHEST.addBlockAlternative(new BlockAlternative((short) 6649, "facing=east", "type=right", "waterlogged=false"));
	}
}
