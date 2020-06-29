package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Chest {
	public static void initStates() {
		CHEST.addBlockAlternative(new BlockAlternative((short) 2034, "facing=north", "type=single", "waterlogged=true"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2035, "facing=north", "type=single", "waterlogged=false"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2036, "facing=north", "type=left", "waterlogged=true"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2037, "facing=north", "type=left", "waterlogged=false"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2038, "facing=north", "type=right", "waterlogged=true"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2039, "facing=north", "type=right", "waterlogged=false"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2040, "facing=south", "type=single", "waterlogged=true"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2041, "facing=south", "type=single", "waterlogged=false"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2042, "facing=south", "type=left", "waterlogged=true"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2043, "facing=south", "type=left", "waterlogged=false"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2044, "facing=south", "type=right", "waterlogged=true"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2045, "facing=south", "type=right", "waterlogged=false"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2046, "facing=west", "type=single", "waterlogged=true"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2047, "facing=west", "type=single", "waterlogged=false"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2048, "facing=west", "type=left", "waterlogged=true"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2049, "facing=west", "type=left", "waterlogged=false"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2050, "facing=west", "type=right", "waterlogged=true"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2051, "facing=west", "type=right", "waterlogged=false"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2052, "facing=east", "type=single", "waterlogged=true"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2053, "facing=east", "type=single", "waterlogged=false"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2054, "facing=east", "type=left", "waterlogged=true"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2055, "facing=east", "type=left", "waterlogged=false"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2056, "facing=east", "type=right", "waterlogged=true"));
		CHEST.addBlockAlternative(new BlockAlternative((short) 2057, "facing=east", "type=right", "waterlogged=false"));
	}
}
