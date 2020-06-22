package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DarkPrismarineSlab {
	public static void initStates() {
		DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7320, "type=top", "waterlogged=true"));
		DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7321, "type=top", "waterlogged=false"));
		DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7322, "type=bottom", "waterlogged=true"));
		DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7323, "type=bottom", "waterlogged=false"));
		DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7324, "type=double", "waterlogged=true"));
		DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7325, "type=double", "waterlogged=false"));
	}
}
