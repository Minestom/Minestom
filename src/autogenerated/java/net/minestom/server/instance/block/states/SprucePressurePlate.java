package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SprucePressurePlate {
	public static void initStates() {
		SPRUCE_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3875, "powered=true"));
		SPRUCE_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3876, "powered=false"));
	}
}
