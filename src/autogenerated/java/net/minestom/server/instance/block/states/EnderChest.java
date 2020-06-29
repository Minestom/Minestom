package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class EnderChest {
	public static void initStates() {
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5235, "facing=north", "waterlogged=true"));
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5236, "facing=north", "waterlogged=false"));
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5237, "facing=south", "waterlogged=true"));
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5238, "facing=south", "waterlogged=false"));
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5239, "facing=west", "waterlogged=true"));
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5240, "facing=west", "waterlogged=false"));
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5241, "facing=east", "waterlogged=true"));
		ENDER_CHEST.addBlockAlternative(new BlockAlternative((short) 5242, "facing=east", "waterlogged=false"));
	}
}
