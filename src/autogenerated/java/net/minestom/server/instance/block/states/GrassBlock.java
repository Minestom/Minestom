package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GrassBlock {
	public static void initStates() {
		GRASS_BLOCK.addBlockAlternative(new BlockAlternative((short) 8, "snowy=true"));
		GRASS_BLOCK.addBlockAlternative(new BlockAlternative((short) 9, "snowy=false"));
	}
}
