package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DeadTubeCoralWallFan {
	public static void initStates() {
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9024, "facing=north", "waterlogged=true"));
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9025, "facing=north", "waterlogged=false"));
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9026, "facing=south", "waterlogged=true"));
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9027, "facing=south", "waterlogged=false"));
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9028, "facing=west", "waterlogged=true"));
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9029, "facing=west", "waterlogged=false"));
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9030, "facing=east", "waterlogged=true"));
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9031, "facing=east", "waterlogged=false"));
	}
}
