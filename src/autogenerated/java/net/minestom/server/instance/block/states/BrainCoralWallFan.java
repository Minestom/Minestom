package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BrainCoralWallFan {
	public static void initStates() {
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9608, "facing=north", "waterlogged=true"));
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9609, "facing=north", "waterlogged=false"));
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9610, "facing=south", "waterlogged=true"));
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9611, "facing=south", "waterlogged=false"));
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9612, "facing=west", "waterlogged=true"));
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9613, "facing=west", "waterlogged=false"));
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9614, "facing=east", "waterlogged=true"));
		BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9615, "facing=east", "waterlogged=false"));
	}
}
