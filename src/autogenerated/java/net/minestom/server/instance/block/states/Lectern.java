package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Lectern {
	public static void initStates() {
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14833, "facing=north", "has_book=true", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14834, "facing=north", "has_book=true", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14835, "facing=north", "has_book=false", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14836, "facing=north", "has_book=false", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14837, "facing=south", "has_book=true", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14838, "facing=south", "has_book=true", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14839, "facing=south", "has_book=false", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14840, "facing=south", "has_book=false", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14841, "facing=west", "has_book=true", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14842, "facing=west", "has_book=true", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14843, "facing=west", "has_book=false", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14844, "facing=west", "has_book=false", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14845, "facing=east", "has_book=true", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14846, "facing=east", "has_book=true", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14847, "facing=east", "has_book=false", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14848, "facing=east", "has_book=false", "powered=false"));
	}
}
