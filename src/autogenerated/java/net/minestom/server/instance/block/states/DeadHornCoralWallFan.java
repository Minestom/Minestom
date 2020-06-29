package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DeadHornCoralWallFan {
	public static void initStates() {
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9056, "facing=north", "waterlogged=true"));
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9057, "facing=north", "waterlogged=false"));
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9058, "facing=south", "waterlogged=true"));
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9059, "facing=south", "waterlogged=false"));
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9060, "facing=west", "waterlogged=true"));
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9061, "facing=west", "waterlogged=false"));
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9062, "facing=east", "waterlogged=true"));
		DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9063, "facing=east", "waterlogged=false"));
	}
}
