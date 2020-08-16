package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class FireCoralWallFan {
	public static void initStates() {
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9628, "facing=north", "waterlogged=true"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9629, "facing=north", "waterlogged=false"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9630, "facing=south", "waterlogged=true"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9631, "facing=south", "waterlogged=false"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9632, "facing=west", "waterlogged=true"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9633, "facing=west", "waterlogged=false"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9634, "facing=east", "waterlogged=true"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9635, "facing=east", "waterlogged=false"));
	}
}
