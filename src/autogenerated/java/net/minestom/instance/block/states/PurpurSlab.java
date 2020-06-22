package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PurpurSlab {
	public static void initStates() {
		PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 7872, "type=top", "waterlogged=true"));
		PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 7873, "type=top", "waterlogged=false"));
		PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 7874, "type=bottom", "waterlogged=true"));
		PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 7875, "type=bottom", "waterlogged=false"));
		PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 7876, "type=double", "waterlogged=true"));
		PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 7877, "type=double", "waterlogged=false"));
	}
}
