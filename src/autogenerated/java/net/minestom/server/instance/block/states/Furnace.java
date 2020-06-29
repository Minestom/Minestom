package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Furnace {
	public static void initStates() {
		FURNACE.addBlockAlternative(new BlockAlternative((short) 3373, "facing=north", "lit=true"));
		FURNACE.addBlockAlternative(new BlockAlternative((short) 3374, "facing=north", "lit=false"));
		FURNACE.addBlockAlternative(new BlockAlternative((short) 3375, "facing=south", "lit=true"));
		FURNACE.addBlockAlternative(new BlockAlternative((short) 3376, "facing=south", "lit=false"));
		FURNACE.addBlockAlternative(new BlockAlternative((short) 3377, "facing=west", "lit=true"));
		FURNACE.addBlockAlternative(new BlockAlternative((short) 3378, "facing=west", "lit=false"));
		FURNACE.addBlockAlternative(new BlockAlternative((short) 3379, "facing=east", "lit=true"));
		FURNACE.addBlockAlternative(new BlockAlternative((short) 3380, "facing=east", "lit=false"));
	}
}
