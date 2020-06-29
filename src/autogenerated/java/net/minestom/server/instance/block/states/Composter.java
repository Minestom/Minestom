package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Composter {
	public static void initStates() {
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15751, "level=0"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15752, "level=1"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15753, "level=2"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15754, "level=3"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15755, "level=4"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15756, "level=5"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15757, "level=6"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15758, "level=7"));
		COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15759, "level=8"));
	}
}
