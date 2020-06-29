package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GraniteSlab {
	public static void initStates() {
		GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10301, "type=top", "waterlogged=true"));
		GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10302, "type=top", "waterlogged=false"));
		GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10303, "type=bottom", "waterlogged=true"));
		GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10304, "type=bottom", "waterlogged=false"));
		GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10305, "type=double", "waterlogged=true"));
		GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10306, "type=double", "waterlogged=false"));
	}
}
