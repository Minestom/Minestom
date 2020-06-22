package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class ChippedAnvil {
	public static void initStates() {
		CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6078, "facing=north"));
		CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6079, "facing=south"));
		CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6080, "facing=west"));
		CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6081, "facing=east"));
	}
}
