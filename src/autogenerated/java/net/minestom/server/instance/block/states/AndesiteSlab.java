package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class AndesiteSlab {
	public static void initStates() {
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10843, "type=top", "waterlogged=true"));
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10844, "type=top", "waterlogged=false"));
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10845, "type=bottom", "waterlogged=true"));
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10846, "type=bottom", "waterlogged=false"));
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10847, "type=double", "waterlogged=true"));
		ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10848, "type=double", "waterlogged=false"));
	}
}
