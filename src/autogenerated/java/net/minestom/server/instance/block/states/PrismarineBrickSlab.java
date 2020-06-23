package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PrismarineBrickSlab {
	public static void initStates() {
		PRISMARINE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 7314, "type=top", "waterlogged=true"));
		PRISMARINE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 7315, "type=top", "waterlogged=false"));
		PRISMARINE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 7316, "type=bottom", "waterlogged=true"));
		PRISMARINE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 7317, "type=bottom", "waterlogged=false"));
		PRISMARINE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 7318, "type=double", "waterlogged=true"));
		PRISMARINE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 7319, "type=double", "waterlogged=false"));
	}
}
