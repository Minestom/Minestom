package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedAcaciaWood {
	public static void initStates() {
		STRIPPED_ACACIA_WOOD.addBlockAlternative(new BlockAlternative((short) 138, "axis=x"));
		STRIPPED_ACACIA_WOOD.addBlockAlternative(new BlockAlternative((short) 139, "axis=y"));
		STRIPPED_ACACIA_WOOD.addBlockAlternative(new BlockAlternative((short) 140, "axis=z"));
	}
}
