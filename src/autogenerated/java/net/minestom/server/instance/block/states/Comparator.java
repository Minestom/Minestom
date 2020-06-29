package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Comparator {
	public static void initStates() {
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6678, "facing=north", "mode=compare", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6679, "facing=north", "mode=compare", "powered=false"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6680, "facing=north", "mode=subtract", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6681, "facing=north", "mode=subtract", "powered=false"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6682, "facing=south", "mode=compare", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6683, "facing=south", "mode=compare", "powered=false"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6684, "facing=south", "mode=subtract", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6685, "facing=south", "mode=subtract", "powered=false"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6686, "facing=west", "mode=compare", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6687, "facing=west", "mode=compare", "powered=false"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6688, "facing=west", "mode=subtract", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6689, "facing=west", "mode=subtract", "powered=false"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6690, "facing=east", "mode=compare", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6691, "facing=east", "mode=compare", "powered=false"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6692, "facing=east", "mode=subtract", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6693, "facing=east", "mode=subtract", "powered=false"));
	}
}
