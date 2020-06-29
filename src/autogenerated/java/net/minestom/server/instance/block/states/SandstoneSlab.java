package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SandstoneSlab {
	public static void initStates() {
		SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8348, "type=top", "waterlogged=true"));
		SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8349, "type=top", "waterlogged=false"));
		SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8350, "type=bottom", "waterlogged=true"));
		SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8351, "type=bottom", "waterlogged=false"));
		SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8352, "type=double", "waterlogged=true"));
		SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8353, "type=double", "waterlogged=false"));
	}
}
