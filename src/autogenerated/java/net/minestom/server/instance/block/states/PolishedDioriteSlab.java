package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PolishedDioriteSlab {
	public static void initStates() {
		POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10807, "type=top", "waterlogged=true"));
		POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10808, "type=top", "waterlogged=false"));
		POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10809, "type=bottom", "waterlogged=true"));
		POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10810, "type=bottom", "waterlogged=false"));
		POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10811, "type=double", "waterlogged=true"));
		POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10812, "type=double", "waterlogged=false"));
	}
}
