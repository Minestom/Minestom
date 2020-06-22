package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BoneBlock {
	public static void initStates() {
		BONE_BLOCK.addBlockAlternative(new BlockAlternative((short) 8720, "axis=x"));
		BONE_BLOCK.addBlockAlternative(new BlockAlternative((short) 8721, "axis=y"));
		BONE_BLOCK.addBlockAlternative(new BlockAlternative((short) 8722, "axis=z"));
	}
}
