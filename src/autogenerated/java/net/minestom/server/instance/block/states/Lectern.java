package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Lectern {
	public static void initStates() {
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14837, "facing=north", "has_book=true", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14838, "facing=north", "has_book=true", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14839, "facing=north", "has_book=false", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14840, "facing=north", "has_book=false", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14841, "facing=south", "has_book=true", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14842, "facing=south", "has_book=true", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14843, "facing=south", "has_book=false", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14844, "facing=south", "has_book=false", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14845, "facing=west", "has_book=true", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14846, "facing=west", "has_book=true", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14847, "facing=west", "has_book=false", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14848, "facing=west", "has_book=false", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14849, "facing=east", "has_book=true", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14850, "facing=east", "has_book=true", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14851, "facing=east", "has_book=false", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 14852, "facing=east", "has_book=false", "powered=false"));
	}
}
