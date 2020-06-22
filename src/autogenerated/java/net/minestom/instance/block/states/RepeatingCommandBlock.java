package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class RepeatingCommandBlock {
	public static void initStates() {
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8689, "conditional=true", "facing=north"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8690, "conditional=true", "facing=east"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8691, "conditional=true", "facing=south"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8692, "conditional=true", "facing=west"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8693, "conditional=true", "facing=up"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8694, "conditional=true", "facing=down"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8695, "conditional=false", "facing=north"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8696, "conditional=false", "facing=east"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8697, "conditional=false", "facing=south"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8698, "conditional=false", "facing=west"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8699, "conditional=false", "facing=up"));
		REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8700, "conditional=false", "facing=down"));
	}
}
