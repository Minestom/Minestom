package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BubbleCoral {
	public static void initStates() {
		BUBBLE_CORAL.addBlockAlternative(new BlockAlternative((short) 8998, "waterlogged=true"));
		BUBBLE_CORAL.addBlockAlternative(new BlockAlternative((short) 8999, "waterlogged=false"));
	}
}
