package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PrismarineSlab {
	public static void initStates() {
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7848, "type=top", "waterlogged=true"));
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7849, "type=top", "waterlogged=false"));
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7850, "type=bottom", "waterlogged=true"));
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7851, "type=bottom", "waterlogged=false"));
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7852, "type=double", "waterlogged=true"));
		PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7853, "type=double", "waterlogged=false"));
	}
}
