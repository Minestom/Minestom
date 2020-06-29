package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class AcaciaSlab {
	public static void initStates() {
		ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8324, "type=top", "waterlogged=true"));
		ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8325, "type=top", "waterlogged=false"));
		ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8326, "type=bottom", "waterlogged=true"));
		ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8327, "type=bottom", "waterlogged=false"));
		ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8328, "type=double", "waterlogged=true"));
		ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8329, "type=double", "waterlogged=false"));
	}
}
