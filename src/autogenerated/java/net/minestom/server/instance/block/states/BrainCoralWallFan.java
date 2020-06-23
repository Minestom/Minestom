package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BrainCoralWallFan {
	public static void initStates() {
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9072, "facing=north", "waterlogged=true"));
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9073, "facing=north", "waterlogged=false"));
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9074, "facing=south", "waterlogged=true"));
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9075, "facing=south", "waterlogged=false"));
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9076, "facing=west", "waterlogged=true"));
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9077, "facing=west", "waterlogged=false"));
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9078, "facing=east", "waterlogged=true"));
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9079, "facing=east", "waterlogged=false"));
	}
}
