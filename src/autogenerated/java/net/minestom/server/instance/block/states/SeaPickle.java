package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SeaPickle {
	public static void initStates() {
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9104, "pickles=1", "waterlogged=true"));
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9105, "pickles=1", "waterlogged=false"));
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9106, "pickles=2", "waterlogged=true"));
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9107, "pickles=2", "waterlogged=false"));
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9108, "pickles=3", "waterlogged=true"));
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9109, "pickles=3", "waterlogged=false"));
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9110, "pickles=4", "waterlogged=true"));
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9111, "pickles=4", "waterlogged=false"));
	}
}
