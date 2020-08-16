package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DeadHornCoralWallFan {
	public static void initStates() {
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9596, "facing=north", "waterlogged=true"));
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9597, "facing=north", "waterlogged=false"));
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9598, "facing=south", "waterlogged=true"));
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9599, "facing=south", "waterlogged=false"));
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9600, "facing=west", "waterlogged=true"));
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9601, "facing=west", "waterlogged=false"));
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9602, "facing=east", "waterlogged=true"));
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9603, "facing=east", "waterlogged=false"));
	}
}
