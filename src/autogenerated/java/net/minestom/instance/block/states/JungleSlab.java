package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class JungleSlab {
	public static void initStates() {
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 7782, "type=top", "waterlogged=true"));
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 7783, "type=top", "waterlogged=false"));
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 7784, "type=bottom", "waterlogged=true"));
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 7785, "type=bottom", "waterlogged=false"));
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 7786, "type=double", "waterlogged=true"));
		JUNGLE_SLAB.addBlockAlternative(new BlockAlternative((short) 7787, "type=double", "waterlogged=false"));
	}
}
