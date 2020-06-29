package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PumpkinStem {
	public static void initStates() {
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4772, "age=0"));
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4773, "age=1"));
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4774, "age=2"));
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4775, "age=3"));
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4776, "age=4"));
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4777, "age=5"));
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4778, "age=6"));
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4779, "age=7"));
	}
}
