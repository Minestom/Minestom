package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class FireCoralFan {
	public static void initStates() {
		FIRE_CORAL_FAN.addBlockAlternative(new BlockAlternative((short) 9020, "waterlogged=true"));
		FIRE_CORAL_FAN.addBlockAlternative(new BlockAlternative((short) 9021, "waterlogged=false"));
	}
}
