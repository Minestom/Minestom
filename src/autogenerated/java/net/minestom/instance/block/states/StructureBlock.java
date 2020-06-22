package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StructureBlock {
	public static void initStates() {
		STRUCTURE_BLOCK.addBlockAlternative(new BlockAlternative((short) 11268, "mode=save"));
		STRUCTURE_BLOCK.addBlockAlternative(new BlockAlternative((short) 11269, "mode=load"));
		STRUCTURE_BLOCK.addBlockAlternative(new BlockAlternative((short) 11270, "mode=corner"));
		STRUCTURE_BLOCK.addBlockAlternative(new BlockAlternative((short) 11271, "mode=data"));
	}
}
