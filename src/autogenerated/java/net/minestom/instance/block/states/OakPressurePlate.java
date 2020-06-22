package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OakPressurePlate {
	public static void initStates() {
		OAK_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3871, "powered=true"));
		OAK_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3872, "powered=false"));
	}
}
