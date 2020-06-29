package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class JungleWood {
	public static void initStates() {
		JUNGLE_WOOD.addBlockAlternative(new BlockAlternative((short) 118, "axis=x"));
		JUNGLE_WOOD.addBlockAlternative(new BlockAlternative((short) 119, "axis=y"));
		JUNGLE_WOOD.addBlockAlternative(new BlockAlternative((short) 120, "axis=z"));
	}
}
