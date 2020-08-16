package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CrimsonSlab {
	public static void initStates() {
		CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15055, "type=top", "waterlogged=true"));
		CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15056, "type=top", "waterlogged=false"));
		CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15057, "type=bottom", "waterlogged=true"));
		CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15058, "type=bottom", "waterlogged=false"));
		CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15059, "type=double", "waterlogged=true"));
		CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15060, "type=double", "waterlogged=false"));
	}
}
