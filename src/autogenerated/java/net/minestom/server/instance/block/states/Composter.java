package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Composter {
	public static void initStates() {
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15759, "level=0"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15760, "level=1"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15761, "level=2"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15762, "level=3"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15763, "level=4"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15764, "level=5"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15765, "level=6"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15766, "level=7"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15767, "level=8"));
	}
}
