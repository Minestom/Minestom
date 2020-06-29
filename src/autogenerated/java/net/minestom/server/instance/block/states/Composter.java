package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Composter {
	public static void initStates() {
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 11278, "level=0"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 11279, "level=1"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 11280, "level=2"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 11281, "level=3"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 11282, "level=4"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 11283, "level=5"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 11284, "level=6"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 11285, "level=7"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 11286, "level=8"));
	}
}
