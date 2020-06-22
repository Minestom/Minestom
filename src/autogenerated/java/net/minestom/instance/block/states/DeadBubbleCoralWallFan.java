package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DeadBubbleCoralWallFan {
	public static void initStates() {
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9040, "facing=north", "waterlogged=true"));
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9041, "facing=north", "waterlogged=false"));
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9042, "facing=south", "waterlogged=true"));
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9043, "facing=south", "waterlogged=false"));
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9044, "facing=west", "waterlogged=true"));
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9045, "facing=west", "waterlogged=false"));
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9046, "facing=east", "waterlogged=true"));
		DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9047, "facing=east", "waterlogged=false"));
	}
}
