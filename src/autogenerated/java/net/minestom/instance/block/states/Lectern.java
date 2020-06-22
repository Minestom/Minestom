package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Lectern {
	public static void initStates() {
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11177, "facing=north", "has_book=true", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11178, "facing=north", "has_book=true", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11179, "facing=north", "has_book=false", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11180, "facing=north", "has_book=false", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11181, "facing=south", "has_book=true", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11182, "facing=south", "has_book=true", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11183, "facing=south", "has_book=false", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11184, "facing=south", "has_book=false", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11185, "facing=west", "has_book=true", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11186, "facing=west", "has_book=true", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11187, "facing=west", "has_book=false", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11188, "facing=west", "has_book=false", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11189, "facing=east", "has_book=true", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11190, "facing=east", "has_book=true", "powered=false"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11191, "facing=east", "has_book=false", "powered=true"));
		LECTERN.addBlockAlternative(new BlockAlternative((short) 11192, "facing=east", "has_book=false", "powered=false"));
	}
}
