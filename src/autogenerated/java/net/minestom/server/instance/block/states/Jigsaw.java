package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Jigsaw {
	public static void initStates() {
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 11272, "facing=north"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 11273, "facing=east"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 11274, "facing=south"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 11275, "facing=west"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 11276, "facing=up"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 11277, "facing=down"));
	}
}
