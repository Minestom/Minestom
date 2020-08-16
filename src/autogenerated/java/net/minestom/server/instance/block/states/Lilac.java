package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Lilac {
	public static void initStates() {
		LILAC.addBlockAlternative(new BlockAlternative((short) 7891, "half=upper"));
		LILAC.addBlockAlternative(new BlockAlternative((short) 7892, "half=lower"));
	}
}
