package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DeadTubeCoralWallFan {
	public static void initStates() {
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9564, "facing=north", "waterlogged=true"));
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9565, "facing=north", "waterlogged=false"));
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9566, "facing=south", "waterlogged=true"));
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9567, "facing=south", "waterlogged=false"));
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9568, "facing=west", "waterlogged=true"));
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9569, "facing=west", "waterlogged=false"));
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9570, "facing=east", "waterlogged=true"));
		DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9571, "facing=east", "waterlogged=false"));
	}
}
