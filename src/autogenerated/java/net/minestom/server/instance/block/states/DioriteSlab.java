package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DioriteSlab {
	public static void initStates() {
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10865, "type=top", "waterlogged=true"));
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10866, "type=top", "waterlogged=false"));
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10867, "type=bottom", "waterlogged=true"));
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10868, "type=bottom", "waterlogged=false"));
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10869, "type=double", "waterlogged=true"));
		DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10870, "type=double", "waterlogged=false"));
	}
}
