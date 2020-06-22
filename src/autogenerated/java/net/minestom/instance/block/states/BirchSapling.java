package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BirchSapling {
	public static void initStates() {
		BIRCH_SAPLING.addBlockAlternative(new BlockAlternative((short) 25, "stage=0"));
		BIRCH_SAPLING.addBlockAlternative(new BlockAlternative((short) 26, "stage=1"));
	}
}
