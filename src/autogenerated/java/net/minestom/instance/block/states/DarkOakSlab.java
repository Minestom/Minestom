package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DarkOakSlab {
	public static void initStates() {
		DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7794, "type=top", "waterlogged=true"));
		DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7795, "type=top", "waterlogged=false"));
		DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7796, "type=bottom", "waterlogged=true"));
		DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7797, "type=bottom", "waterlogged=false"));
		DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7798, "type=double", "waterlogged=true"));
		DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7799, "type=double", "waterlogged=false"));
	}
}
