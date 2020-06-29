package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DarkPrismarineSlab {
	public static void initStates() {
		DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7856, "type=top", "waterlogged=true"));
		DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7857, "type=top", "waterlogged=false"));
		DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7858, "type=bottom", "waterlogged=true"));
		DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7859, "type=bottom", "waterlogged=false"));
		DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7860, "type=double", "waterlogged=true"));
		DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7861, "type=double", "waterlogged=false"));
	}
}
