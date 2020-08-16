package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class MossyCobblestoneSlab {
	public static void initStates() {
		MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10817, "type=top", "waterlogged=true"));
		MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10818, "type=top", "waterlogged=false"));
		MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10819, "type=bottom", "waterlogged=true"));
		MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10820, "type=bottom", "waterlogged=false"));
		MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10821, "type=double", "waterlogged=true"));
		MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10822, "type=double", "waterlogged=false"));
	}
}
