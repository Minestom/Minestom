package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlackstoneSlab {
	public static void initStates() {
		BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16252, "type=top", "waterlogged=true"));
		BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16253, "type=top", "waterlogged=false"));
		BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16254, "type=bottom", "waterlogged=true"));
		BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16255, "type=bottom", "waterlogged=false"));
		BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16256, "type=double", "waterlogged=true"));
		BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16257, "type=double", "waterlogged=false"));
	}
}
