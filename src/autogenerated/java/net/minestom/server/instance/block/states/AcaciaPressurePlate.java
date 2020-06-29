package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class AcaciaPressurePlate {
	public static void initStates() {
		ACACIA_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3881, "powered=true"));
		ACACIA_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3882, "powered=false"));
	}
}
