package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Lantern {
	public static void initStates() {
		LANTERN.addBlockAlternative(new BlockAlternative((short) 11230, "hanging=true"));
		LANTERN.addBlockAlternative(new BlockAlternative((short) 11231, "hanging=false"));
	}
}
