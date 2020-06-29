package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlackstoneSlab {
	public static void initStates() {
		BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16244, "type=top", "waterlogged=true"));
		BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16245, "type=top", "waterlogged=false"));
		BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16246, "type=bottom", "waterlogged=true"));
		BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16247, "type=bottom", "waterlogged=false"));
		BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16248, "type=double", "waterlogged=true"));
		BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16249, "type=double", "waterlogged=false"));
	}
}
