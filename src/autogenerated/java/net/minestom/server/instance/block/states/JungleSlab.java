package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class JungleSlab {
	public static void initStates() {
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 8318, "type=top", "waterlogged=true"));
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 8319, "type=top", "waterlogged=false"));
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 8320, "type=bottom", "waterlogged=true"));
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 8321, "type=bottom", "waterlogged=false"));
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 8322, "type=double", "waterlogged=true"));
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 8323, "type=double", "waterlogged=false"));
	}
}
