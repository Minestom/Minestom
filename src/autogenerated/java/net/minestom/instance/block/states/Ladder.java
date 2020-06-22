package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Ladder {
	public static void initStates() {
		LADDER.addBlockAlternative(new BlockAlternative((short) 3635, "facing=north", "waterlogged=true"));
		LADDER.addBlockAlternative(new BlockAlternative((short) 3636, "facing=north", "waterlogged=false"));
		LADDER.addBlockAlternative(new BlockAlternative((short) 3637, "facing=south", "waterlogged=true"));
		LADDER.addBlockAlternative(new BlockAlternative((short) 3638, "facing=south", "waterlogged=false"));
		LADDER.addBlockAlternative(new BlockAlternative((short) 3639, "facing=west", "waterlogged=true"));
		LADDER.addBlockAlternative(new BlockAlternative((short) 3640, "facing=west", "waterlogged=false"));
		LADDER.addBlockAlternative(new BlockAlternative((short) 3641, "facing=east", "waterlogged=true"));
		LADDER.addBlockAlternative(new BlockAlternative((short) 3642, "facing=east", "waterlogged=false"));
	}
}
