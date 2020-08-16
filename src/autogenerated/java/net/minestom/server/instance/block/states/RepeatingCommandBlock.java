package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class RepeatingCommandBlock {
	public static void initStates() {
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9229, "conditional=true", "facing=north"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9230, "conditional=true", "facing=east"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9231, "conditional=true", "facing=south"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9232, "conditional=true", "facing=west"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9233, "conditional=true", "facing=up"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9234, "conditional=true", "facing=down"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9235, "conditional=false", "facing=north"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9236, "conditional=false", "facing=east"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9237, "conditional=false", "facing=south"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9238, "conditional=false", "facing=west"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9239, "conditional=false", "facing=up"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9240, "conditional=false", "facing=down"));
	}
}
