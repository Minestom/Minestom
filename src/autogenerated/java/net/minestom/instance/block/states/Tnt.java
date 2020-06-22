package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Tnt {
	public static void initStates() {
		TNT.addBlockAlternative(new BlockAlternative((short) 1429, "unstable=true"));
		TNT.addBlockAlternative(new BlockAlternative((short) 1430, "unstable=false"));
	}
}
