package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Sunflower {
	public static void initStates() {
		SUNFLOWER.addBlockAlternative(new BlockAlternative((short) 7349, "half=upper"));
		SUNFLOWER.addBlockAlternative(new BlockAlternative((short) 7350, "half=lower"));
	}
}
