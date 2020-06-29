package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DeadFireCoralWallFan {
	public static void initStates() {
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9584, "facing=north", "waterlogged=true"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9585, "facing=north", "waterlogged=false"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9586, "facing=south", "waterlogged=true"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9587, "facing=south", "waterlogged=false"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9588, "facing=west", "waterlogged=true"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9589, "facing=west", "waterlogged=false"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9590, "facing=east", "waterlogged=true"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9591, "facing=east", "waterlogged=false"));
	}
}
