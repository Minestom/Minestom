package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Smoker {
	public static void initStates() {
		SMOKER.addBlockAlternative(new BlockAlternative((short) 11147, "facing=north", "lit=true"));
		SMOKER.addBlockAlternative(new BlockAlternative((short) 11148, "facing=north", "lit=false"));
		SMOKER.addBlockAlternative(new BlockAlternative((short) 11149, "facing=south", "lit=true"));
		SMOKER.addBlockAlternative(new BlockAlternative((short) 11150, "facing=south", "lit=false"));
		SMOKER.addBlockAlternative(new BlockAlternative((short) 11151, "facing=west", "lit=true"));
		SMOKER.addBlockAlternative(new BlockAlternative((short) 11152, "facing=west", "lit=false"));
		SMOKER.addBlockAlternative(new BlockAlternative((short) 11153, "facing=east", "lit=true"));
		SMOKER.addBlockAlternative(new BlockAlternative((short) 11154, "facing=east", "lit=false"));
	}
}
