package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SandstoneSlab {
	public static void initStates() {
		SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7812, "type=top", "waterlogged=true"));
		SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7813, "type=top", "waterlogged=false"));
		SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7814, "type=bottom", "waterlogged=true"));
		SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7815, "type=bottom", "waterlogged=false"));
		SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7816, "type=double", "waterlogged=true"));
		SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 7817, "type=double", "waterlogged=false"));
	}
}
