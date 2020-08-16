package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BrainCoral {
	public static void initStates() {
		BRAIN_CORAL.addBlockAlternative(new BlockAlternative((short) 9536, "waterlogged=true"));
		BRAIN_CORAL.addBlockAlternative(new BlockAlternative((short) 9537, "waterlogged=false"));
	}
}
