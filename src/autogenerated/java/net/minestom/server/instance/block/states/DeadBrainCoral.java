package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DeadBrainCoral {
	public static void initStates() {
		DEAD_BRAIN_CORAL.addBlockAlternative(new BlockAlternative((short) 9526, "waterlogged=true"));
		DEAD_BRAIN_CORAL.addBlockAlternative(new BlockAlternative((short) 9527, "waterlogged=false"));
	}
}
