package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DeadBrainCoralWallFan {
	public static void initStates() {
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9568, "facing=north", "waterlogged=true"));
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9569, "facing=north", "waterlogged=false"));
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9570, "facing=south", "waterlogged=true"));
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9571, "facing=south", "waterlogged=false"));
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9572, "facing=west", "waterlogged=true"));
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9573, "facing=west", "waterlogged=false"));
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9574, "facing=east", "waterlogged=true"));
		DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9575, "facing=east", "waterlogged=false"));
	}
}
