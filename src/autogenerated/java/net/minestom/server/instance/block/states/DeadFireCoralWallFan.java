package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DeadFireCoralWallFan {
	public static void initStates() {
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9588, "facing=north", "waterlogged=true"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9589, "facing=north", "waterlogged=false"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9590, "facing=south", "waterlogged=true"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9591, "facing=south", "waterlogged=false"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9592, "facing=west", "waterlogged=true"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9593, "facing=west", "waterlogged=false"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9594, "facing=east", "waterlogged=true"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9595, "facing=east", "waterlogged=false"));
	}
}
