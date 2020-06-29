package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class HornCoralWallFan {
	public static void initStates() {
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9632, "facing=north", "waterlogged=true"));
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9633, "facing=north", "waterlogged=false"));
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9634, "facing=south", "waterlogged=true"));
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9635, "facing=south", "waterlogged=false"));
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9636, "facing=west", "waterlogged=true"));
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9637, "facing=west", "waterlogged=false"));
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9638, "facing=east", "waterlogged=true"));
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9639, "facing=east", "waterlogged=false"));
	}
}
