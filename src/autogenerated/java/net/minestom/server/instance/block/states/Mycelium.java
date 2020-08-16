package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Mycelium {
	public static void initStates() {
		MYCELIUM.addBlockAlternative(new BlockAlternative((short) 5016, "snowy=true"));
		MYCELIUM.addBlockAlternative(new BlockAlternative((short) 5017, "snowy=false"));
	}
}
