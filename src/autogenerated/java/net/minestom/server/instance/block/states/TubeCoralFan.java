package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class TubeCoralFan {
	public static void initStates() {
		TUBE_CORAL_FAN.addBlockAlternative(new BlockAlternative((short) 9554, "waterlogged=true"));
		TUBE_CORAL_FAN.addBlockAlternative(new BlockAlternative((short) 9555, "waterlogged=false"));
	}
}
