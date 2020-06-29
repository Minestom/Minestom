package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class TubeCoral {
	public static void initStates() {
		TUBE_CORAL.addBlockAlternative(new BlockAlternative((short) 9530, "waterlogged=true"));
		TUBE_CORAL.addBlockAlternative(new BlockAlternative((short) 9531, "waterlogged=false"));
	}
}
