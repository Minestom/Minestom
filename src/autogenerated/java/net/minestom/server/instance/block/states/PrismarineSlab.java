package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PrismarineSlab {
	public static void initStates() {
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7844, "type=top", "waterlogged=true"));
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7845, "type=top", "waterlogged=false"));
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7846, "type=bottom", "waterlogged=true"));
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7847, "type=bottom", "waterlogged=false"));
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7848, "type=double", "waterlogged=true"));
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7849, "type=double", "waterlogged=false"));
	}
}
