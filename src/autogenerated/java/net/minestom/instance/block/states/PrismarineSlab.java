package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PrismarineSlab {
	public static void initStates() {
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7308, "type=top", "waterlogged=true"));
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7309, "type=top", "waterlogged=false"));
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7310, "type=bottom", "waterlogged=true"));
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7311, "type=bottom", "waterlogged=false"));
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7312, "type=double", "waterlogged=true"));
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7313, "type=double", "waterlogged=false"));
	}
}
