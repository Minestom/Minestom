package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PolishedAndesiteSlab {
	public static void initStates() {
		POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10319, "type=top", "waterlogged=true"));
		POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10320, "type=top", "waterlogged=false"));
		POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10321, "type=bottom", "waterlogged=true"));
		POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10322, "type=bottom", "waterlogged=false"));
		POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10323, "type=double", "waterlogged=true"));
		POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10324, "type=double", "waterlogged=false"));
	}
}
