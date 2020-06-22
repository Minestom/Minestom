package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DeadBrainCoralWallFan {
	public static void initStates() {
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9032, "facing=north", "waterlogged=true"));
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9033, "facing=north", "waterlogged=false"));
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9034, "facing=south", "waterlogged=true"));
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9035, "facing=south", "waterlogged=false"));
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9036, "facing=west", "waterlogged=true"));
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9037, "facing=west", "waterlogged=false"));
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9038, "facing=east", "waterlogged=true"));
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9039, "facing=east", "waterlogged=false"));
	}
}
