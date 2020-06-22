package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class ChainCommandBlock {
	public static void initStates() {
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8701, "conditional=true", "facing=north"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8702, "conditional=true", "facing=east"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8703, "conditional=true", "facing=south"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8704, "conditional=true", "facing=west"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8705, "conditional=true", "facing=up"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8706, "conditional=true", "facing=down"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8707, "conditional=false", "facing=north"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8708, "conditional=false", "facing=east"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8709, "conditional=false", "facing=south"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8710, "conditional=false", "facing=west"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8711, "conditional=false", "facing=up"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 8712, "conditional=false", "facing=down"));
	}
}
