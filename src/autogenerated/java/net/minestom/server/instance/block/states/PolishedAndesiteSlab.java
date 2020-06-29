package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PolishedAndesiteSlab {
	public static void initStates() {
		POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10855, "type=top", "waterlogged=true"));
		POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10856, "type=top", "waterlogged=false"));
		POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10857, "type=bottom", "waterlogged=true"));
		POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10858, "type=bottom", "waterlogged=false"));
		POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10859, "type=double", "waterlogged=true"));
		POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10860, "type=double", "waterlogged=false"));
	}
}
