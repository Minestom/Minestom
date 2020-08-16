package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class JungleSlab {
	public static void initStates() {
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 8322, "type=top", "waterlogged=true"));
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 8323, "type=top", "waterlogged=false"));
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 8324, "type=bottom", "waterlogged=true"));
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 8325, "type=bottom", "waterlogged=false"));
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 8326, "type=double", "waterlogged=true"));
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 8327, "type=double", "waterlogged=false"));
	}
}
