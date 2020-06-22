package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DeadFireCoralFan {
	public static void initStates() {
		DEAD_FIRE_CORAL_FAN.addBlockAlternative(new BlockAlternative((short) 9010, "waterlogged=true"));
		DEAD_FIRE_CORAL_FAN.addBlockAlternative(new BlockAlternative((short) 9011, "waterlogged=false"));
	}
}
