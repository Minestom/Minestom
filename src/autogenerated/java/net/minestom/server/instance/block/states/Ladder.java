package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Ladder {
	public static void initStates() {
		LADDER.addBlockAlternative(new BlockAlternative((short) 3637, "facing=north", "waterlogged=true"));
		LADDER.addBlockAlternative(new BlockAlternative((short) 3638, "facing=north", "waterlogged=false"));
		LADDER.addBlockAlternative(new BlockAlternative((short) 3639, "facing=south", "waterlogged=true"));
		LADDER.addBlockAlternative(new BlockAlternative((short) 3640, "facing=south", "waterlogged=false"));
		LADDER.addBlockAlternative(new BlockAlternative((short) 3641, "facing=west", "waterlogged=true"));
		LADDER.addBlockAlternative(new BlockAlternative((short) 3642, "facing=west", "waterlogged=false"));
		LADDER.addBlockAlternative(new BlockAlternative((short) 3643, "facing=east", "waterlogged=true"));
		LADDER.addBlockAlternative(new BlockAlternative((short) 3644, "facing=east", "waterlogged=false"));
	}
}
