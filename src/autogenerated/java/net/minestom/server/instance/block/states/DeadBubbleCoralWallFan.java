package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DeadBubbleCoralWallFan {
	public static void initStates() {
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9580, "facing=north", "waterlogged=true"));
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9581, "facing=north", "waterlogged=false"));
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9582, "facing=south", "waterlogged=true"));
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9583, "facing=south", "waterlogged=false"));
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9584, "facing=west", "waterlogged=true"));
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9585, "facing=west", "waterlogged=false"));
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9586, "facing=east", "waterlogged=true"));
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9587, "facing=east", "waterlogged=false"));
	}
}
