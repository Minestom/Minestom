package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DarkOakPressurePlate {
	public static void initStates() {
		DARK_OAK_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3883, "powered=true"));
		DARK_OAK_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3884, "powered=false"));
	}
}
