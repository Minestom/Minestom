package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class AndesiteSlab {
	public static void initStates() {
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10307, "type=top", "waterlogged=true"));
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10308, "type=top", "waterlogged=false"));
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10309, "type=bottom", "waterlogged=true"));
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10310, "type=bottom", "waterlogged=false"));
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10311, "type=double", "waterlogged=true"));
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10312, "type=double", "waterlogged=false"));
	}
}
