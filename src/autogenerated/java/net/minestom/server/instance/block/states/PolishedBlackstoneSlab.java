package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PolishedBlackstoneSlab {
	public static void initStates() {
		POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16745, "type=top", "waterlogged=true"));
		POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16746, "type=top", "waterlogged=false"));
		POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16747, "type=bottom", "waterlogged=true"));
		POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16748, "type=bottom", "waterlogged=false"));
		POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16749, "type=double", "waterlogged=true"));
		POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16750, "type=double", "waterlogged=false"));
	}
}
