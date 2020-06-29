package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class AcaciaSapling {
	public static void initStates() {
		ACACIA_SAPLING.addBlockAlternative(new BlockAlternative((short) 29, "stage=0"));
		ACACIA_SAPLING.addBlockAlternative(new BlockAlternative((short) 30, "stage=1"));
	}
}
