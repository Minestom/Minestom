package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class QuartzSlab {
	public static void initStates() {
		QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 7854, "type=top", "waterlogged=true"));
		QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 7855, "type=top", "waterlogged=false"));
		QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 7856, "type=bottom", "waterlogged=true"));
		QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 7857, "type=bottom", "waterlogged=false"));
		QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 7858, "type=double", "waterlogged=true"));
		QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 7859, "type=double", "waterlogged=false"));
	}
}
