package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PetrifiedOakSlab {
	public static void initStates() {
		PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7824, "type=top", "waterlogged=true"));
		PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7825, "type=top", "waterlogged=false"));
		PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7826, "type=bottom", "waterlogged=true"));
		PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7827, "type=bottom", "waterlogged=false"));
		PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7828, "type=double", "waterlogged=true"));
		PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 7829, "type=double", "waterlogged=false"));
	}
}
