package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SpruceSapling {
	public static void initStates() {
		SPRUCE_SAPLING.addBlockAlternative(new BlockAlternative((short) 23, "stage=0"));
		SPRUCE_SAPLING.addBlockAlternative(new BlockAlternative((short) 24, "stage=1"));
	}
}
