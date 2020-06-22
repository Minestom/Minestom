package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class MossyCobblestoneSlab {
	public static void initStates() {
		MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10277, "type=top", "waterlogged=true"));
		MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10278, "type=top", "waterlogged=false"));
		MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10279, "type=bottom", "waterlogged=true"));
		MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10280, "type=bottom", "waterlogged=false"));
		MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10281, "type=double", "waterlogged=true"));
		MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10282, "type=double", "waterlogged=false"));
	}
}
