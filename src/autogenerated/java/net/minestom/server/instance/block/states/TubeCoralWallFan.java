package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class TubeCoralWallFan {
	public static void initStates() {
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9604, "facing=north", "waterlogged=true"));
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9605, "facing=north", "waterlogged=false"));
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9606, "facing=south", "waterlogged=true"));
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9607, "facing=south", "waterlogged=false"));
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9608, "facing=west", "waterlogged=true"));
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9609, "facing=west", "waterlogged=false"));
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9610, "facing=east", "waterlogged=true"));
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9611, "facing=east", "waterlogged=false"));
	}
}
