package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class AcaciaSlab {
	public static void initStates() {
		ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 7788, "type=top", "waterlogged=true"));
		ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 7789, "type=top", "waterlogged=false"));
		ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 7790, "type=bottom", "waterlogged=true"));
		ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 7791, "type=bottom", "waterlogged=false"));
		ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 7792, "type=double", "waterlogged=true"));
		ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 7793, "type=double", "waterlogged=false"));
	}
}
