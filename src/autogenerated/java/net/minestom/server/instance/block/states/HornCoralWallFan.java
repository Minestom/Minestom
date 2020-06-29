package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class HornCoralWallFan {
	public static void initStates() {
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9096, "facing=north", "waterlogged=true"));
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9097, "facing=north", "waterlogged=false"));
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9098, "facing=south", "waterlogged=true"));
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9099, "facing=south", "waterlogged=false"));
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9100, "facing=west", "waterlogged=true"));
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9101, "facing=west", "waterlogged=false"));
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9102, "facing=east", "waterlogged=true"));
		HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9103, "facing=east", "waterlogged=false"));
	}
}
