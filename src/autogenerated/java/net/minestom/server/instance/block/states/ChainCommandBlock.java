package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class ChainCommandBlock {
	public static void initStates() {
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9237, "conditional=true", "facing=north"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9238, "conditional=true", "facing=east"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9239, "conditional=true", "facing=south"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9240, "conditional=true", "facing=west"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9241, "conditional=true", "facing=up"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9242, "conditional=true", "facing=down"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9243, "conditional=false", "facing=north"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9244, "conditional=false", "facing=east"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9245, "conditional=false", "facing=south"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9246, "conditional=false", "facing=west"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9247, "conditional=false", "facing=up"));
		CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9248, "conditional=false", "facing=down"));
	}
}
