package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class JungleSapling {
	public static void initStates() {
		JUNGLE_SAPLING.addBlockAlternative(new BlockAlternative((short) 27, "stage=0"));
		JUNGLE_SAPLING.addBlockAlternative(new BlockAlternative((short) 28, "stage=1"));
	}
}
