package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class HornCoral {
	public static void initStates() {
		HORN_CORAL.addBlockAlternative(new BlockAlternative((short) 9542, "waterlogged=true"));
		HORN_CORAL.addBlockAlternative(new BlockAlternative((short) 9543, "waterlogged=false"));
	}
}
