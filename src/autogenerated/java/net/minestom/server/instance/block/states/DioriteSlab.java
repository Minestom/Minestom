package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DioriteSlab {
	public static void initStates() {
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10861, "type=top", "waterlogged=true"));
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10862, "type=top", "waterlogged=false"));
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10863, "type=bottom", "waterlogged=true"));
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10864, "type=bottom", "waterlogged=false"));
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10865, "type=double", "waterlogged=true"));
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10866, "type=double", "waterlogged=false"));
	}
}
