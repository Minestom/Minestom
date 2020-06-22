package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BubbleCoralWallFan {
	public static void initStates() {
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9080, "facing=north", "waterlogged=true"));
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9081, "facing=north", "waterlogged=false"));
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9082, "facing=south", "waterlogged=true"));
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9083, "facing=south", "waterlogged=false"));
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9084, "facing=west", "waterlogged=true"));
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9085, "facing=west", "waterlogged=false"));
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9086, "facing=east", "waterlogged=true"));
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9087, "facing=east", "waterlogged=false"));
	}
}
