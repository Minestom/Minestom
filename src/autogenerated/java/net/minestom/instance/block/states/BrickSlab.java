package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BrickSlab {
	public static void initStates() {
		BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 7836, "type=top", "waterlogged=true"));
		BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 7837, "type=top", "waterlogged=false"));
		BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 7838, "type=bottom", "waterlogged=true"));
		BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 7839, "type=bottom", "waterlogged=false"));
		BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 7840, "type=double", "waterlogged=true"));
		BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 7841, "type=double", "waterlogged=false"));
	}
}
