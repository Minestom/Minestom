package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Wheat {
	public static void initStates() {
		WHEAT.addBlockAlternative(new BlockAlternative((short) 3355, "age=0"));
		WHEAT.addBlockAlternative(new BlockAlternative((short) 3356, "age=1"));
		WHEAT.addBlockAlternative(new BlockAlternative((short) 3357, "age=2"));
		WHEAT.addBlockAlternative(new BlockAlternative((short) 3358, "age=3"));
		WHEAT.addBlockAlternative(new BlockAlternative((short) 3359, "age=4"));
		WHEAT.addBlockAlternative(new BlockAlternative((short) 3360, "age=5"));
		WHEAT.addBlockAlternative(new BlockAlternative((short) 3361, "age=6"));
		WHEAT.addBlockAlternative(new BlockAlternative((short) 3362, "age=7"));
	}
}
