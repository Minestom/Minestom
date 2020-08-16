package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OakSlab {
	public static void initStates() {
		OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8304, "type=top", "waterlogged=true"));
		OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8305, "type=top", "waterlogged=false"));
		OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8306, "type=bottom", "waterlogged=true"));
		OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8307, "type=bottom", "waterlogged=false"));
		OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8308, "type=double", "waterlogged=true"));
		OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8309, "type=double", "waterlogged=false"));
	}
}
