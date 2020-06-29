package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class JunglePressurePlate {
	public static void initStates() {
		JUNGLE_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3879, "powered=true"));
		JUNGLE_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3880, "powered=false"));
	}
}
