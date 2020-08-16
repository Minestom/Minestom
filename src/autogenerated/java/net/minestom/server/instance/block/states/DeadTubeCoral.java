package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DeadTubeCoral {
	public static void initStates() {
		DEAD_TUBE_CORAL.addBlockAlternative(new BlockAlternative((short) 9524, "waterlogged=true"));
		DEAD_TUBE_CORAL.addBlockAlternative(new BlockAlternative((short) 9525, "waterlogged=false"));
	}
}
