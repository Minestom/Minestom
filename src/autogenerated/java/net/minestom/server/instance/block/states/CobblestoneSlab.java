package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CobblestoneSlab {
	public static void initStates() {
		COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7830, "type=top", "waterlogged=true"));
		COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7831, "type=top", "waterlogged=false"));
		COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7832, "type=bottom", "waterlogged=true"));
		COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7833, "type=bottom", "waterlogged=false"));
		COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7834, "type=double", "waterlogged=true"));
		COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7835, "type=double", "waterlogged=false"));
	}
}
