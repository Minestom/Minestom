package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class QuartzSlab {
	public static void initStates() {
		QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8390, "type=top", "waterlogged=true"));
		QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8391, "type=top", "waterlogged=false"));
		QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8392, "type=bottom", "waterlogged=true"));
		QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8393, "type=bottom", "waterlogged=false"));
		QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8394, "type=double", "waterlogged=true"));
		QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8395, "type=double", "waterlogged=false"));
	}
}
