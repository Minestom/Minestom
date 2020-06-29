package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StructureBlock {
	public static void initStates() {
		STRUCTURE_BLOCK.addBlockAlternative(new BlockAlternative((short) 15735, "mode=save"));
		STRUCTURE_BLOCK.addBlockAlternative(new BlockAlternative((short) 15736, "mode=load"));
		STRUCTURE_BLOCK.addBlockAlternative(new BlockAlternative((short) 15737, "mode=corner"));
		STRUCTURE_BLOCK.addBlockAlternative(new BlockAlternative((short) 15738, "mode=data"));
	}
}
