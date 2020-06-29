package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class RedstoneOre {
	public static void initStates() {
		REDSTONE_ORE.addBlockAlternative(new BlockAlternative((short) 3885, "lit=true"));
		REDSTONE_ORE.addBlockAlternative(new BlockAlternative((short) 3886, "lit=false"));
	}
}
