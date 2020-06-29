package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class EnderChest {
	public static void initStates() {
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5251, "facing=north", "waterlogged=true"));
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5252, "facing=north", "waterlogged=false"));
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5253, "facing=south", "waterlogged=true"));
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5254, "facing=south", "waterlogged=false"));
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5255, "facing=west", "waterlogged=true"));
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5256, "facing=west", "waterlogged=false"));
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5257, "facing=east", "waterlogged=true"));
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5258, "facing=east", "waterlogged=false"));
	}
}
