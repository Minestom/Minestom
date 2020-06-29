package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Beetroots {
	public static void initStates() {
		BEETROOTS.addBlockAlternative(new BlockAlternative((short) 8683, "age=0"));
		BEETROOTS.addBlockAlternative(new BlockAlternative((short) 8684, "age=1"));
		BEETROOTS.addBlockAlternative(new BlockAlternative((short) 8685, "age=2"));
		BEETROOTS.addBlockAlternative(new BlockAlternative((short) 8686, "age=3"));
	}
}
