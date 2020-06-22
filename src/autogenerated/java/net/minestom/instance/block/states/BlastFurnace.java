package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlastFurnace {
	public static void initStates() {
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 11155, "facing=north", "lit=true"));
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 11156, "facing=north", "lit=false"));
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 11157, "facing=south", "lit=true"));
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 11158, "facing=south", "lit=false"));
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 11159, "facing=west", "lit=true"));
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 11160, "facing=west", "lit=false"));
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 11161, "facing=east", "lit=true"));
		BLAST_FURNACE.addBlockAlternative(new BlockAlternative((short) 11162, "facing=east", "lit=false"));
	}
}
