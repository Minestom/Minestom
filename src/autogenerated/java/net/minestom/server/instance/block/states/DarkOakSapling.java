package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DarkOakSapling {
	public static void initStates() {
		DARK_OAK_SAPLING.addBlockAlternative(new BlockAlternative((short) 31, "stage=0"));
		DARK_OAK_SAPLING.addBlockAlternative(new BlockAlternative((short) 32, "stage=1"));
	}
}
