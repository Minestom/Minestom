package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Chain {
	public static void initStates() {
		CHAIN.addBlockAlternative(new BlockAlternative((short) 4729, "axis=x", "waterlogged=true"));
		CHAIN.addBlockAlternative(new BlockAlternative((short) 4730, "axis=x", "waterlogged=false"));
		CHAIN.addBlockAlternative(new BlockAlternative((short) 4731, "axis=y", "waterlogged=true"));
		CHAIN.addBlockAlternative(new BlockAlternative((short) 4732, "axis=y", "waterlogged=false"));
		CHAIN.addBlockAlternative(new BlockAlternative((short) 4733, "axis=z", "waterlogged=true"));
		CHAIN.addBlockAlternative(new BlockAlternative((short) 4734, "axis=z", "waterlogged=false"));
	}
}
