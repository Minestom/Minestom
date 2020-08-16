package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Lantern {
	public static void initStates() {
		LANTERN.addBlockAlternative(new BlockAlternative((short) 14890, "hanging=true", "waterlogged=true"));
		LANTERN.addBlockAlternative(new BlockAlternative((short) 14891, "hanging=true", "waterlogged=false"));
		LANTERN.addBlockAlternative(new BlockAlternative((short) 14892, "hanging=false", "waterlogged=true"));
		LANTERN.addBlockAlternative(new BlockAlternative((short) 14893, "hanging=false", "waterlogged=false"));
	}
}
