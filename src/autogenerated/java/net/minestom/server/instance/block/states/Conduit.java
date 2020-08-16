package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Conduit {
	public static void initStates() {
		CONDUIT.addBlockAlternative(new BlockAlternative((short) 9653, "waterlogged=true"));
		CONDUIT.addBlockAlternative(new BlockAlternative((short) 9654, "waterlogged=false"));
	}
}
