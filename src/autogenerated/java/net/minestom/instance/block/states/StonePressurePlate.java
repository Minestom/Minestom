package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StonePressurePlate {
	public static void initStates() {
		STONE_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3805, "powered=true"));
		STONE_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3806, "powered=false"));
	}
}
