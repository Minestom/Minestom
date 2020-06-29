package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PurpurSlab {
	public static void initStates() {
		PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8408, "type=top", "waterlogged=true"));
		PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8409, "type=top", "waterlogged=false"));
		PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8410, "type=bottom", "waterlogged=true"));
		PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8411, "type=bottom", "waterlogged=false"));
		PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8412, "type=double", "waterlogged=true"));
		PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8413, "type=double", "waterlogged=false"));
	}
}
