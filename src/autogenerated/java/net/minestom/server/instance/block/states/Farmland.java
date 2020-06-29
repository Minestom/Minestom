package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Farmland {
	public static void initStates() {
		FARMLAND.addBlockAlternative(new BlockAlternative((short) 3365, "moisture=0"));
		FARMLAND.addBlockAlternative(new BlockAlternative((short) 3366, "moisture=1"));
		FARMLAND.addBlockAlternative(new BlockAlternative((short) 3367, "moisture=2"));
		FARMLAND.addBlockAlternative(new BlockAlternative((short) 3368, "moisture=3"));
		FARMLAND.addBlockAlternative(new BlockAlternative((short) 3369, "moisture=4"));
		FARMLAND.addBlockAlternative(new BlockAlternative((short) 3370, "moisture=5"));
		FARMLAND.addBlockAlternative(new BlockAlternative((short) 3371, "moisture=6"));
		FARMLAND.addBlockAlternative(new BlockAlternative((short) 3372, "moisture=7"));
	}
}
