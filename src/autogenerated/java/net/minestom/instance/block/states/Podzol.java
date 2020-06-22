package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Podzol {
	public static void initStates() {
		PODZOL.addBlockAlternative(new BlockAlternative((short) 12, "snowy=true"));
		PODZOL.addBlockAlternative(new BlockAlternative((short) 13, "snowy=false"));
	}
}
