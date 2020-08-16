package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CrimsonPressurePlate {
	public static void initStates() {
		CRIMSON_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 15067, "powered=true"));
		CRIMSON_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 15068, "powered=false"));
	}
}
