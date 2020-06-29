package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BrainCoralFan {
	public static void initStates() {
		BRAIN_CORAL_FAN.addBlockAlternative(new BlockAlternative((short) 9016, "waterlogged=true"));
		BRAIN_CORAL_FAN.addBlockAlternative(new BlockAlternative((short) 9017, "waterlogged=false"));
	}
}
