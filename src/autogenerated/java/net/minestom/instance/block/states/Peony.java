package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Peony {
	public static void initStates() {
		PEONY.addBlockAlternative(new BlockAlternative((short) 7355, "half=upper"));
		PEONY.addBlockAlternative(new BlockAlternative((short) 7356, "half=lower"));
	}
}
