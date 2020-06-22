package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class JunglePressurePlate {
	public static void initStates() {
		JUNGLE_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3877, "powered=true"));
		JUNGLE_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3878, "powered=false"));
	}
}
