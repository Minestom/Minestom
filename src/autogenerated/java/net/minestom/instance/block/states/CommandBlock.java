package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CommandBlock {
	public static void initStates() {
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5628, "conditional=true", "facing=north"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5629, "conditional=true", "facing=east"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5630, "conditional=true", "facing=south"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5631, "conditional=true", "facing=west"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5632, "conditional=true", "facing=up"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5633, "conditional=true", "facing=down"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5634, "conditional=false", "facing=north"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5635, "conditional=false", "facing=east"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5636, "conditional=false", "facing=south"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5637, "conditional=false", "facing=west"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5638, "conditional=false", "facing=up"));
		COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5639, "conditional=false", "facing=down"));
	}
}
