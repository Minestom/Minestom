package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StrippedJungleWood {
	public static void initStates() {
		STRIPPED_JUNGLE_WOOD.addBlockAlternative(new BlockAlternative((short) 135, "axis=x"));
		STRIPPED_JUNGLE_WOOD.addBlockAlternative(new BlockAlternative((short) 136, "axis=y"));
		STRIPPED_JUNGLE_WOOD.addBlockAlternative(new BlockAlternative((short) 137, "axis=z"));
	}
}
