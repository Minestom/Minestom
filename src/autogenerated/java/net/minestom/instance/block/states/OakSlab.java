package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OakSlab {
	public static void initStates() {
		OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7764, "type=top", "waterlogged=true"));
		OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7765, "type=top", "waterlogged=false"));
		OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7766, "type=bottom", "waterlogged=true"));
		OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7767, "type=bottom", "waterlogged=false"));
		OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7768, "type=double", "waterlogged=true"));
		OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7769, "type=double", "waterlogged=false"));
	}
}
