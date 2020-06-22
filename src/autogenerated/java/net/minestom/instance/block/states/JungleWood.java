package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class JungleWood {
	public static void initStates() {
		JUNGLE_WOOD.addBlockAlternative(new BlockAlternative((short) 117, "axis=x"));
		JUNGLE_WOOD.addBlockAlternative(new BlockAlternative((short) 118, "axis=y"));
		JUNGLE_WOOD.addBlockAlternative(new BlockAlternative((short) 119, "axis=z"));
	}
}
