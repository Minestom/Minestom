package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SpruceSlab {
	public static void initStates() {
		SPRUCE_SLAB.addBlockAlternative(new BlockAlternative((short) 7770, "type=top", "waterlogged=true"));
		SPRUCE_SLAB.addBlockAlternative(new BlockAlternative((short) 7771, "type=top", "waterlogged=false"));
		SPRUCE_SLAB.addBlockAlternative(new BlockAlternative((short) 7772, "type=bottom", "waterlogged=true"));
		SPRUCE_SLAB.addBlockAlternative(new BlockAlternative((short) 7773, "type=bottom", "waterlogged=false"));
		SPRUCE_SLAB.addBlockAlternative(new BlockAlternative((short) 7774, "type=double", "waterlogged=true"));
		SPRUCE_SLAB.addBlockAlternative(new BlockAlternative((short) 7775, "type=double", "waterlogged=false"));
	}
}
