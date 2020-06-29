package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PolishedGraniteSlab {
	public static void initStates() {
		POLISHED_GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10253, "type=top", "waterlogged=true"));
		POLISHED_GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10254, "type=top", "waterlogged=false"));
		POLISHED_GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10255, "type=bottom", "waterlogged=true"));
		POLISHED_GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10256, "type=bottom", "waterlogged=false"));
		POLISHED_GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10257, "type=double", "waterlogged=true"));
		POLISHED_GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10258, "type=double", "waterlogged=false"));
	}
}
