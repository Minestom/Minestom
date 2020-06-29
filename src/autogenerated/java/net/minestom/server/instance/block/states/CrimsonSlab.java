package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CrimsonSlab {
	public static void initStates() {
		CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15047, "type=top", "waterlogged=true"));
		CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15048, "type=top", "waterlogged=false"));
		CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15049, "type=bottom", "waterlogged=true"));
		CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15050, "type=bottom", "waterlogged=false"));
		CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15051, "type=double", "waterlogged=true"));
		CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15052, "type=double", "waterlogged=false"));
	}
}
