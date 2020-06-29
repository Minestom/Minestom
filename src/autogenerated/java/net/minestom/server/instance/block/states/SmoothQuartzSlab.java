package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SmoothQuartzSlab {
	public static void initStates() {
		SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10831, "type=top", "waterlogged=true"));
		SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10832, "type=top", "waterlogged=false"));
		SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10833, "type=bottom", "waterlogged=true"));
		SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10834, "type=bottom", "waterlogged=false"));
		SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10835, "type=double", "waterlogged=true"));
		SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10836, "type=double", "waterlogged=false"));
	}
}
