package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SmoothQuartzSlab {
	public static void initStates() {
		SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10835, "type=top", "waterlogged=true"));
		SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10836, "type=top", "waterlogged=false"));
		SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10837, "type=bottom", "waterlogged=true"));
		SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10838, "type=bottom", "waterlogged=false"));
		SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10839, "type=double", "waterlogged=true"));
		SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10840, "type=double", "waterlogged=false"));
	}
}
