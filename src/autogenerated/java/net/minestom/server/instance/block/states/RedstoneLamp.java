package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class RedstoneLamp {
	public static void initStates() {
		REDSTONE_LAMP.addBlockAlternative(new BlockAlternative((short) 5160, "lit=true"));
		REDSTONE_LAMP.addBlockAlternative(new BlockAlternative((short) 5161, "lit=false"));
	}
}
