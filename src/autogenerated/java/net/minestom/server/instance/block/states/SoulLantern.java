package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SoulLantern {
	public static void initStates() {
		SOUL_LANTERN.addBlockAlternative(new BlockAlternative((short) 14894, "hanging=true", "waterlogged=true"));
		SOUL_LANTERN.addBlockAlternative(new BlockAlternative((short) 14895, "hanging=true", "waterlogged=false"));
		SOUL_LANTERN.addBlockAlternative(new BlockAlternative((short) 14896, "hanging=false", "waterlogged=true"));
		SOUL_LANTERN.addBlockAlternative(new BlockAlternative((short) 14897, "hanging=false", "waterlogged=false"));
	}
}
