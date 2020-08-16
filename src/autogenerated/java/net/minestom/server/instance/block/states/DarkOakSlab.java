package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DarkOakSlab {
	public static void initStates() {
		DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8334, "type=top", "waterlogged=true"));
		DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8335, "type=top", "waterlogged=false"));
		DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8336, "type=bottom", "waterlogged=true"));
		DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8337, "type=bottom", "waterlogged=false"));
		DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8338, "type=double", "waterlogged=true"));
		DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8339, "type=double", "waterlogged=false"));
	}
}
