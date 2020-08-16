package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BoneBlock {
	public static void initStates() {
		BONE_BLOCK.addBlockAlternative(new BlockAlternative((short) 9260, "axis=x"));
		BONE_BLOCK.addBlockAlternative(new BlockAlternative((short) 9261, "axis=y"));
		BONE_BLOCK.addBlockAlternative(new BlockAlternative((short) 9262, "axis=z"));
	}
}
