package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Smoker {
	public static void initStates() {
		SMOKER.addBlockAlternative(new BlockAlternative((short) 14803, "facing=north", "lit=true"));
		SMOKER.addBlockAlternative(new BlockAlternative((short) 14804, "facing=north", "lit=false"));
		SMOKER.addBlockAlternative(new BlockAlternative((short) 14805, "facing=south", "lit=true"));
		SMOKER.addBlockAlternative(new BlockAlternative((short) 14806, "facing=south", "lit=false"));
		SMOKER.addBlockAlternative(new BlockAlternative((short) 14807, "facing=west", "lit=true"));
		SMOKER.addBlockAlternative(new BlockAlternative((short) 14808, "facing=west", "lit=false"));
		SMOKER.addBlockAlternative(new BlockAlternative((short) 14809, "facing=east", "lit=true"));
		SMOKER.addBlockAlternative(new BlockAlternative((short) 14810, "facing=east", "lit=false"));
	}
}
