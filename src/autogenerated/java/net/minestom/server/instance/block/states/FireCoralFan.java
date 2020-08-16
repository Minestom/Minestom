package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class FireCoralFan {
	public static void initStates() {
		FIRE_CORAL_FAN.addBlockAlternative(new BlockAlternative((short) 9560, "waterlogged=true"));
		FIRE_CORAL_FAN.addBlockAlternative(new BlockAlternative((short) 9561, "waterlogged=false"));
	}
}
