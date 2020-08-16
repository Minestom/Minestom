package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class AndesiteSlab {
	public static void initStates() {
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10847, "type=top", "waterlogged=true"));
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10848, "type=top", "waterlogged=false"));
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10849, "type=bottom", "waterlogged=true"));
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10850, "type=bottom", "waterlogged=false"));
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10851, "type=double", "waterlogged=true"));
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10852, "type=double", "waterlogged=false"));
	}
}
