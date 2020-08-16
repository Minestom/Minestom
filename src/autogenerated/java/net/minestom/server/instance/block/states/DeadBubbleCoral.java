package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DeadBubbleCoral {
	public static void initStates() {
		DEAD_BUBBLE_CORAL.addBlockAlternative(new BlockAlternative((short) 9528, "waterlogged=true"));
		DEAD_BUBBLE_CORAL.addBlockAlternative(new BlockAlternative((short) 9529, "waterlogged=false"));
	}
}
