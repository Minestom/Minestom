package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DeadFireCoralWallFan {
	public static void initStates() {
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9048, "facing=north", "waterlogged=true"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9049, "facing=north", "waterlogged=false"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9050, "facing=south", "waterlogged=true"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9051, "facing=south", "waterlogged=false"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9052, "facing=west", "waterlogged=true"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9053, "facing=west", "waterlogged=false"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9054, "facing=east", "waterlogged=true"));
		DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9055, "facing=east", "waterlogged=false"));
	}
}
