package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CobblestoneSlab {
	public static void initStates() {
		COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8366, "type=top", "waterlogged=true"));
		COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8367, "type=top", "waterlogged=false"));
		COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8368, "type=bottom", "waterlogged=true"));
		COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8369, "type=bottom", "waterlogged=false"));
		COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8370, "type=double", "waterlogged=true"));
		COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8371, "type=double", "waterlogged=false"));
	}
}
