package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PetrifiedOakSlab {
	public static void initStates() {
		PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8364, "type=top", "waterlogged=true"));
		PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8365, "type=top", "waterlogged=false"));
		PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8366, "type=bottom", "waterlogged=true"));
		PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8367, "type=bottom", "waterlogged=false"));
		PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8368, "type=double", "waterlogged=true"));
		PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8369, "type=double", "waterlogged=false"));
	}
}
