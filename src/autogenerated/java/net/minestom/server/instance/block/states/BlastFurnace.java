package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlastFurnace {
	public static void initStates() {
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 14815, "facing=north", "lit=true"));
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 14816, "facing=north", "lit=false"));
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 14817, "facing=south", "lit=true"));
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 14818, "facing=south", "lit=false"));
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 14819, "facing=west", "lit=true"));
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 14820, "facing=west", "lit=false"));
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 14821, "facing=east", "lit=true"));
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 14822, "facing=east", "lit=false"));
	}
}
