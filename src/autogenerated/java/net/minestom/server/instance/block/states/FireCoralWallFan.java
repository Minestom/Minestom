package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class FireCoralWallFan {
	public static void initStates() {
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9624, "facing=north", "waterlogged=true"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9625, "facing=north", "waterlogged=false"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9626, "facing=south", "waterlogged=true"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9627, "facing=south", "waterlogged=false"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9628, "facing=west", "waterlogged=true"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9629, "facing=west", "waterlogged=false"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9630, "facing=east", "waterlogged=true"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9631, "facing=east", "waterlogged=false"));
	}
}
