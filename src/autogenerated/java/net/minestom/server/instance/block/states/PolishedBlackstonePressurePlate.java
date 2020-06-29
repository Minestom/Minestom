package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PolishedBlackstonePressurePlate {
	public static void initStates() {
		POLISHED_BLACKSTONE_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 16751, "powered=true"));
		POLISHED_BLACKSTONE_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 16752, "powered=false"));
	}
}
