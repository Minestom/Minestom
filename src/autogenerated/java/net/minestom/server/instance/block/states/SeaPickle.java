package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SeaPickle {
	public static void initStates() {
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9640, "pickles=1", "waterlogged=true"));
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9641, "pickles=1", "waterlogged=false"));
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9642, "pickles=2", "waterlogged=true"));
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9643, "pickles=2", "waterlogged=false"));
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9644, "pickles=3", "waterlogged=true"));
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9645, "pickles=3", "waterlogged=false"));
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9646, "pickles=4", "waterlogged=true"));
		SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9647, "pickles=4", "waterlogged=false"));
	}
}
