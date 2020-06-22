package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class TubeCoral {
	public static void initStates() {
		TUBE_CORAL.addBlockAlternative(new BlockAlternative((short) 8994, "waterlogged=true"));
		TUBE_CORAL.addBlockAlternative(new BlockAlternative((short) 8995, "waterlogged=false"));
	}
}
