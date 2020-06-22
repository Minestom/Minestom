package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SprucePressurePlate {
	public static void initStates() {
		SPRUCE_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3873, "powered=true"));
		SPRUCE_PRESSURE_PLATE.addBlockAlternative(new BlockAlternative((short) 3874, "powered=false"));
	}
}
