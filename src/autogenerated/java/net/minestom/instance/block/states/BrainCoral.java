package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BrainCoral {
	public static void initStates() {
		BRAIN_CORAL.addBlockAlternative(new BlockAlternative((short) 8996, "waterlogged=true"));
		BRAIN_CORAL.addBlockAlternative(new BlockAlternative((short) 8997, "waterlogged=false"));
	}
}
