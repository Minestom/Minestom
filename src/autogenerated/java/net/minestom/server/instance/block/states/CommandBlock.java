package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CommandBlock {
	public static void initStates() {
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5648, "conditional=true", "facing=north"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5649, "conditional=true", "facing=east"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5650, "conditional=true", "facing=south"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5651, "conditional=true", "facing=west"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5652, "conditional=true", "facing=up"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5653, "conditional=true", "facing=down"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5654, "conditional=false", "facing=north"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5655, "conditional=false", "facing=east"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5656, "conditional=false", "facing=south"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5657, "conditional=false", "facing=west"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5658, "conditional=false", "facing=up"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5659, "conditional=false", "facing=down"));
	}
}
