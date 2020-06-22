package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PumpkinStem {
	public static void initStates() {
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4756, "age=0"));
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4757, "age=1"));
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4758, "age=2"));
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4759, "age=3"));
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4760, "age=4"));
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4761, "age=5"));
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4762, "age=6"));
		PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4763, "age=7"));
	}
}
