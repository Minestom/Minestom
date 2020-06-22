package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class FireCoralWallFan {
	public static void initStates() {
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9088, "facing=north", "waterlogged=true"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9089, "facing=north", "waterlogged=false"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9090, "facing=south", "waterlogged=true"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9091, "facing=south", "waterlogged=false"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9092, "facing=west", "waterlogged=true"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9093, "facing=west", "waterlogged=false"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9094, "facing=east", "waterlogged=true"));
		FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9095, "facing=east", "waterlogged=false"));
	}
}
