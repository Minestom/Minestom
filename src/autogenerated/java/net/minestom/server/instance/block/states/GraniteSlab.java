package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GraniteSlab {
	public static void initStates() {
		GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10841, "type=top", "waterlogged=true"));
		GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10842, "type=top", "waterlogged=false"));
		GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10843, "type=bottom", "waterlogged=true"));
		GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10844, "type=bottom", "waterlogged=false"));
		GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10845, "type=double", "waterlogged=true"));
		GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10846, "type=double", "waterlogged=false"));
	}
}
