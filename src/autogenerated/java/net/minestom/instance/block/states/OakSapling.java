package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OakSapling {
	public static void initStates() {
		OAK_SAPLING.addBlockAlternative(new BlockAlternative((short) 21, "stage=0"));
		OAK_SAPLING.addBlockAlternative(new BlockAlternative((short) 22, "stage=1"));
	}
}
