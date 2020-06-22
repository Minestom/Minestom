package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class TubeCoralWallFan {
	public static void initStates() {
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9064, "facing=north", "waterlogged=true"));
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9065, "facing=north", "waterlogged=false"));
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9066, "facing=south", "waterlogged=true"));
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9067, "facing=south", "waterlogged=false"));
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9068, "facing=west", "waterlogged=true"));
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9069, "facing=west", "waterlogged=false"));
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9070, "facing=east", "waterlogged=true"));
		TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9071, "facing=east", "waterlogged=false"));
	}
}
