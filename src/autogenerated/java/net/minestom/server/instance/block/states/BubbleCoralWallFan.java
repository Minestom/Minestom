package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BubbleCoralWallFan {
	public static void initStates() {
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9616, "facing=north", "waterlogged=true"));
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9617, "facing=north", "waterlogged=false"));
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9618, "facing=south", "waterlogged=true"));
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9619, "facing=south", "waterlogged=false"));
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9620, "facing=west", "waterlogged=true"));
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9621, "facing=west", "waterlogged=false"));
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9622, "facing=east", "waterlogged=true"));
		BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9623, "facing=east", "waterlogged=false"));
	}
}
